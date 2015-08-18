package kanjiranking;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opencsv.CSVReader;

public class KoohiiReader {

	public static class KoohiiEntry {
		String index = null;
		String character = null;
		String keyword = null;
		String primitive = null;
		String story = null;

		@Override
		public String toString() {
			return toShortString();
		}

		public String toShortString() {
			return character + ", " + (keyword != null ? keyword : "")
					+ (keyword != null && primitive != null ? " " : "")
					+ (primitive != null ? "(" + primitive + ")" : "");
		}

		public String toLongString() {
			return toShortString() + ": " + story;
		}
	}

	List<KoohiiEntry> entries = new ArrayList<>();
	HashMap<String, KoohiiEntry> map = new HashMap<>();

	public void add(KoohiiEntry ke) {
		if (ke != null) {
			entries.add(ke);
			map.put(ke.character, ke);
		}
	}

	public static void readFromFile(KoohiiReader kr, String file) {
		try (CSVReader reader = new CSVReader(new FileReader(file))) {
			String[] fields;
			while ((fields = reader.readNext()) != null) {
				if (fields.length <= 1)
					continue;
				if (fields.length != 6) {
					System.out.println("Warning: wrong number of elements in line " + Arrays.toString(fields));
					continue;
				}

				KoohiiEntry ke = new KoohiiEntry();
				ke.index = fields[0];
				ke.character = fields[1];
				ke.keyword = fields[2];
				ke.story = fields[5];
				kr.parseStory(ke);
				kr.add(ke);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static KoohiiReader readFromFile(String file) {
		KoohiiReader kr = new KoohiiReader();
		readFromFile(kr, file);
		return kr;
	}

	Pattern p = Pattern.compile("@([^@\\(\\)\n]*)\\(([^)]*)\\)");

	private void parseStory(KoohiiEntry ke) {
		StringBuilder story = new StringBuilder();

		Matcher m = p.matcher(ke.story);
		int lastpos = 0;
		while (m.find()) {
			story.append(ke.story.substring(lastpos, m.start()));
			switch (m.group(1)) {
			case "thanks":
				break;
			case "primitive":
				String arg = m.group(2);
				int splitpos = arg.indexOf(':');
				if (splitpos < 0) {
					ke.primitive = arg;
				} else {
					KoohiiEntry primitive = new KoohiiEntry();
					String firstPart = arg.substring(0, splitpos).trim();
					primitive.story = arg.substring(splitpos + 1).trim();

					splitpos = firstPart.indexOf("|");
					if (splitpos < 0) {
						primitive.primitive = firstPart;
						primitive.character = firstPart;
						primitive.index = primitive.character;
					} else {
						primitive.primitive = firstPart.substring(splitpos + 1).trim();
						primitive.character = firstPart.substring(0, splitpos).trim();
						primitive.index = primitive.primitive;
					}
					add(primitive);
				}

				break;
			default:
				// System.out.println(m.group(0));
			}
			lastpos = m.end();
		}
		story.append(ke.story.substring(lastpos));

		ke.story = story.toString().trim();
	}

	/*public static void main(String[] args) {
		String file = "/home/tr/Studium/sonstiges/languages/jap/kanji.koohii.com/my_stories.csv";

		KoohiiReader kr = readFromFile(file);

		// System.out.println(kr.entries);
		for (int i = 0; i < kr.entries.size(); i++) {
			KoohiiEntry ke = kr.entries.get(i);
			System.out.println(ke.character);
		}
	}*/

}
