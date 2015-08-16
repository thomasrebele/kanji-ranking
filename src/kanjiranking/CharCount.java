package kanjiranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

public class CharCount {
	public HashMap<String, CharStat> mrm = new HashMap<String, CharStat>();
	public long count = 0;

	DecimalFormat df;

	public CharCount() {
		df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		df.setMaximumFractionDigits(4);
	}

	public class CharStat {

		public int other = 0;

		public CharStat(String c) {
			this.key = c;
		}

		@Override
		public String toString() {
			return "" + key + " (" + printFrequency() + "%)";
		}

		protected String key;

		protected long count = 1;

		public String getChar() {
			return key;
		}

		public long getCount() {
			return count;
		}

		public double getFrequency() {
			return ((double) count) / CharCount.this.count;
		}

		public String printFrequency() {
			return df.format(getFrequency() * 100);
		}

		public void add(CharStat cstat) {
			this.count += cstat.count;
		}
	}

	public void add(String ch) {
		this.count += 1;
		mrm.compute(ch, (k, v) -> {
			if (v == null)
				return new CharStat(k);
			v.count += 1;
			return v;
		});
	}

	public long getCount() {
		return count;
	}

	public static boolean isHanCharacter(char c) {
		boolean isKanji = false;
		isKanji |= (0x4e00 <= c && c <= 0x9fff); // CJK unified ideographs
		isKanji |= (0x3400 <= c && c <= 0x4dbf); // ... extension A
		isKanji |= (0x20000 <= c && c <= 0x2a6df); // ... extension B
		isKanji |= (0x2a700 <= c && c <= 0x2b73f); // ... extension C
		isKanji |= (0x2b740 <= c && c <= 0x2b81f); // ... extension D
		isKanji |= (0x2b820 <= c && c <= 0x2ceaf); // ... extension E

		isKanji |= (0x3300 <= c && c <= 0x33FF); // CJK compatibility
		isKanji |= (0xfe30 <= c && c <= 0xfe4f); // ... forms
		isKanji |= (0xf900 <= c && c <= 0xfaff); // ... ideographs
		isKanji |= (0x2f800 <= c && c <= 0x2fa1f); // ... ideographs supplement

		return isKanji;
	}

	public static CharCount readFromFile(File f) {
		CharCount result = new CharCount();
		try {
			BufferedReader br = Files.newBufferedReader(f.toPath());

			String line = null;
			while ((line = br.readLine()) != null) {
				String fields[] = line.split("\t");
				// System.out.println(line);
				if (fields.length < 5) {
					if (fields.length > 1) {
						System.out.println("wrong number of fields in line '" + line + "', should be 4");
					}
					continue;
				}
				if (fields[4].equals("count")) {
					continue;
				}

				CharStat cs = result.new CharStat(fields[0]);
				cs.count = Integer.parseInt(fields[4]);
				result.mrm.put(cs.key, cs);
				result.count += cs.count;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void countFromFile(String filename) {
		CharCount hcc = new CharCount();
		try {

			InputStream is;
			if (filename == null) {
				is = System.in;
			} else {
				is = Files.newInputStream(Paths.get(filename));
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			int ch = 0;
			while ((ch = br.read()) >= 0) {

				if (!isHanCharacter((char) ch))
					continue;
				hcc.add("" + (char) ch);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		ArrayList<CharStat> sorted = new ArrayList<CharStat>();
		for (Entry<String, CharStat> e : hcc.mrm.entrySet()) {
			sorted.add(e.getValue());
		}

		Collections.sort(sorted, (c1, c2) -> -Float.compare(c1.count, c2.count));

		System.out.println("character\trank\tfrequency (%)\tcumulative frequency (%)\tcount");
		int pos = 0;
		double cumulative = 0;
		for (CharStat k : sorted) {
			StringBuilder line = new StringBuilder();
			line.append(k.key + "\t");
			line.append((pos++) + "\t");
			line.append(k.getFrequency() + "\t");
			line.append((cumulative += k.getFrequency()) + "\t");
			line.append(k.count);
			System.out.println(line);
		}
		System.out.println();

	}

	public static void main(String... args) {
		String file = null;
		if (args.length == 0) {
			file = null;
		} else {
			file = args[0];
		}
		// file = "/home/tr/Studium/sonstiges/languages/jap/frequency/all.txt";

		countFromFile(file);

		/*String file = "/home/tr/Studium/sonstiges/languages/jap/software/kanji-ranking/data/wiki/wikipedia-kanji-frequency.txt";
		CharCount cc = readFromFile(Paths.get(file).toFile());
		
		System.out.println(cc.mrm.get('鼻'));
		System.out.println(cc.mrm.get('龍'));
		System.out.println(cc.mrm.get('年'));*/
	}

}
