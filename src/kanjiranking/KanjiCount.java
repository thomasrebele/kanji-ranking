package kanjiranking;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class KanjiCount {
	
	public static class Kanji {
		
		public Kanji(char c) {
			this.character = c;
		}
		
		public String toString() {
			return ""+character;
		}
		
		public char character;
		public float count = 1;
	}
	
	public static boolean isKanji(char c) {
		boolean isKanji = false;
		if(0x4e00 <= c && c <= 0x9faf) isKanji = true;
		if(0x3400 <= c && c <= 0x4dbf) isKanji = true;
		if(0x20000 <= c && c <= 0x2a6df) isKanji = true;
		if(0x2a700 <= c && c <= 0x2b73f) isKanji = true;
		if(0x2b740 <= c && c <= 0x2b81f) isKanji = true;
		if(0xf900 <= c && c <= 0xfaff) isKanji = true;

		return isKanji;
	}
	
	public static void main(String...args) {
		String file = "/home/tr/Studium/sonstiges/languages/jap/frequency/all.txt";
		
		int count = 0;
		HashMap<Character, Kanji> mrm = new HashMap<Character, Kanji>();
		try {
			BufferedReader is = Files.newBufferedReader(Paths.get(file));
			
			int ch = 0;
			while((ch = is.read()) >= 0) {
				
				if(!isKanji((char)ch)) continue;
				if(count++ % 10000 == 0) {
					//System.out.println(count / 1024.0 + " char " + ch);
				}
				mrm.compute((char)ch, (k,v) -> {
					if(v == null) return new Kanji(k);
					v.count += 1; return v;
				});
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<Kanji> sorted = new ArrayList<Kanji>();
		for(Entry<Character, Kanji> e : mrm.entrySet()) {
			e.getValue().count /= count;
			sorted.add(e.getValue());
		}
	
		Collections.sort(sorted, (c1, c2) -> -Float.compare(c1.count, c2.count));
		
		int pos=0;
		float cumulative = 0;
		for(Kanji k : sorted) {
			//System.out.println(k.character + " " + (pos++) + " " + k.count + " " + (cumulative += k.count) + " " + k.count*count);
			System.out.print(k.character);
		}
		System.out.println();
	}
	
}
