package kanjiranking.chise;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import kanjiranking.chise.Ideogram.Type;

public class ChiseReader {
	// ideographic description sequence characters
	HashMap<Character, IDSChar> idsMap = new HashMap<>();
	HashMap<Ideogram, Ideogram> map = new HashMap<>();

	String lastDecomposition = null;

	public ChiseReader() {
		String idsChars = "⿰⿱⿴⿵⿶⿷⿸⿹⿺⿻⿲⿳";
		for (int i = 0; i < idsChars.length(); i++) {
			char c = idsChars.charAt(i);
			idsMap.put(c, new IDSChar(c, i < 10 ? 2 : 3));
		}
	}

	public IDSChar idschar(String key) {
		if (key.length() < 1)
			return null;
		return idsMap.get(key.charAt(0));
	}

	public Ideogram ideogram(String key) {
		return ideogram(key, null, -1);
	}

	public Ideogram ideogram(String key, IDSChar ids, int pos) {
		if (key.contains("?")) {
			calculateComponents(lastDecomposition, 0, true);
			return null;
		}

		Ideogram keyig = new Ideogram();
		keyig.key = key;
		keyig.ids = ids;
		keyig.position = pos;

		Ideogram result = map.get(keyig);
		if (result == null) {
			map.put(keyig, keyig);
			result = keyig;
		}

		if (result.position != keyig.position || result.ids != keyig.ids || result.key != keyig.key) {
			if (result.key != null && result.key.equals(keyig.key)) {

			} else if (result.ids != null && result.ids.equals(keyig.ids)) {
				System.out.println("different ids objects");
			} else {
				System.out.println(
						"error? " + result + " " + keyig + " " + keyig.equals(result) + " " + result.equals(keyig));
			}
		}

		return result;
	}

	void calculateComponents(Ideogram ig) {
		if (ig.components == null) {
			lastDecomposition = ig.decomposition;
			Ideogram child = calculateComponents(ig.decomposition, 0, false).found;
			ig.addComponent(child);
		}
	}

	public class CalcCompHelper {
		int nextpos = 0;
		Ideogram found = null;

		public CalcCompHelper(int pos, Ideogram found) {
			this.nextpos = pos;
			this.found = found;
		}
	}

	CalcCompHelper calculateComponents(String s, int pos, boolean debug) {
		if (debug) {
			pos = pos + 0;
		}
		if (pos >= s.length()) {
			System.out.println("calculating components of  " + s + " needs more characters");
			return new CalcCompHelper(0, null);
		}
		// System.out.println("substr " + s.substring(pos));
		// check for special sign
		char c = s.charAt(pos);
		if (c == '&') {
			int until = s.indexOf(';', pos + 1);
			String name = s.substring(pos, until + 1);
			Ideogram subig = ideogram(name, null, -1);
			subig.type = Type.Special;
			return new CalcCompHelper(until + 1, subig);
		}
		IDSChar ids = idsMap.get(c);
		if (ids != null) {
			// System.out.println("ids " + ids);
			int until = pos + 1;
			ArrayList<Ideogram> components = new ArrayList<>();
			for (int i = 0; i < ids.argCount; i++) {
				CalcCompHelper ccHelper = calculateComponents(s, until, debug);
				Ideogram subig = ccHelper.found;
				Ideogram containerig = ideogram(subig.key, ids, i);
				containerig.type = Type.Container;
				containerig.decomposition = subig.key;
				containerig.addComponent(subig);
				components.add(containerig);
				until = ccHelper.nextpos;
			}
			String name = s.substring(pos, until);
			Ideogram parentig = ideogram(name, null, -1);
			parentig.type = Type.Aggregate;
			parentig.decomposition = name;
			for (Ideogram comp : components) {
				parentig.addComponent(comp);
			}
			return new CalcCompHelper(until, parentig);
		} else {
			String name = s.substring(pos, s.offsetByCodePoints(pos, 1));
			Ideogram subig = ideogram(name, null, -1);
			subig.type = Type.Unicode;
			return new CalcCompHelper(pos + name.length(), subig);
		}
	}

	public void readFile(File f) {
		try {
			BufferedReader br = Files.newBufferedReader(f.toPath());
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(";;"))
					continue;
				String[] fields = line.split("\t");

				if (fields.length < 3) {
					continue;
				}

				Ideogram ig = ideogram(fields[1], null, -1);
				if (fields[1].length() == 1) {
					ig.type = Type.Unicode;
				}
				ig.keyDesc = fields[0];

				if (fields.length != 3) {
					// treat additional fields as alternative decomposition
					ig.alternativeDecompositions = new ArrayList<>();
					for (int i = 3; i < fields.length; i++) {
						ig.alternativeDecompositions.add(fields[i]);
					}
				}

				ig.decomposition = fields[2];
				ig.components = null;
				calculateComponents(ig);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class IdeogramCount implements Comparable<IdeogramCount> {
		Ideogram i = null;
		int count = 0;

		@Override
		public int compareTo(IdeogramCount o) {
			if (o == null)
				return -1;
			return -Integer.compare(this.count, o.count);
		}

		@Override
		public String toString() {
			return i + " (" + count + ")";
		}
	}

	public class Stats {

		HashMap<Ideogram, IdeogramCount> componentCount;
		HashSet<Ideogram> specials;

		List<IdeogramCount> componentsByOccurences;
	}

	Stats getStatistics(String kanji) {
		Stats result = new Stats();
		// count occurrences of components
		result.componentCount = new HashMap<>();
		result.specials = new HashSet<>();
		for (int i = 0; i < kanji.length(); i++) {
			Ideogram ig = ideogram("" + kanji.charAt(i), null, -1);
			Collection<Ideogram> components = ig.getAllComponents();

			for (Ideogram comp : components) {
				result.componentCount.compute(comp, (k, v) -> {
					if (v == null) {
						IdeogramCount igc = new IdeogramCount();
						igc.i = k;
						igc.count = 1;
						return igc;
					} else {
						v.count++;
						return v;
					}
				});

				if (comp.type == Type.Special) {
					result.specials.add(comp);
				}
			}
		}
		result.componentsByOccurences = new ArrayList<>();
		for (Entry<Ideogram, IdeogramCount> e : result.componentCount.entrySet()) {
			result.componentsByOccurences.add(e.getValue());
		}
		Collections.sort(result.componentsByOccurences);
		return result;
	}

	public Ideogram parseIdeogram(String string) {
		if (string == null || string.length() == 0)
			return null;

		if (string.indexOf('_') > 0) {
			String first = string.substring(0, 1);
			IDSChar ids = idschar(first);

			int position = 0, i;
			for (i = 1; i < string.length(); i++) {
				if (string.charAt(i) != '_') {
					position = i - 1;
					break;
				}
			}
			int nextUnderscore = string.indexOf('_', i);
			int end = nextUnderscore < 0 ? string.length() : nextUnderscore;
			String key = string.substring(i, end);
			return ideogram(key, ids, position);
		} else {
			return ideogram(string);
		}
	}

}
