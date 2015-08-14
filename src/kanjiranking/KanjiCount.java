package kanjiranking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

public class KanjiCount {

	public static class Kanji {

		public Kanji(char c) {
			this.character = c;
		}

		@Override
		public String toString() {
			return "" + character;
		}

		public char character;

		public long count = 1;
		public double frequency = 0;
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

	public static void main(String... args) {
		String file = "/home/tr/Studium/sonstiges/languages/jap/frequency/all.txt";
		file = "-";

		HashMap<Character, Kanji> mrm = new HashMap<Character, Kanji>();
		try {

			InputStream is;
			if ("-".equals(file)) {
				is = System.in;
			} else {
				is = Files.newInputStream(Paths.get(file));
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			int ch = 0;
			while ((ch = br.read()) >= 0) {

				if (!isHanCharacter((char) ch))
					continue;
				mrm.compute((char) ch, (k, v) -> {
					if (v == null)
						return new Kanji(k);
					v.count += 1;
					return v;
				});
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		int count = 0;
		for (Entry<Character, Kanji> e : mrm.entrySet()) {
			count += e.getValue().count;
		}

		ArrayList<Kanji> sorted = new ArrayList<Kanji>();
		for (Entry<Character, Kanji> e : mrm.entrySet()) {
			e.getValue().frequency = ((double) e.getValue().count) / count;
			sorted.add(e.getValue());
		}

		Collections.sort(sorted, (c1, c2) -> -Float.compare(c1.count, c2.count));

		System.out.println("character\trank\tfrequency (%)\tcumulative frequency (%)\tcount");
		int pos = 0;
		double cumulative = 0;
		for (Kanji k : sorted) {
			StringBuilder line = new StringBuilder();
			line.append(k.character + "\t");
			line.append((pos++) + "\t");
			line.append(k.frequency + "\t");
			line.append((cumulative += k.frequency) + "\t");
			line.append(k.count);
			System.out.println(line);
		}
		System.out.println();
	}

}
