package kanjiranking.chise;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.IntStream;

public class Ranking {
	public HashSet<Ideogram> contained = new HashSet<>();
	public ArrayList<Ideogram> list = new ArrayList<>();

	public HashMap<Ideogram, String> ideogramInfo = null;

	@Override
	public String toString() {
		return list.toString();
	}

	public void add(Ideogram ig) {
		if (ig != null) {
			contained.add(ig);
			list.add(ig);
		}
	}

	public static Ranking readFromString(String str, ChiseReader cr) {
		Ranking result = new Ranking();
		IntStream is = str.codePoints();
		is.forEach(i -> result.add(cr.ideogram(new String(Character.toChars(i)))));
		return result;
	}

	public static Ranking readFromFile(String file) {
		return readFromFile(new Ranking(), file, new ChiseReader());
	}

	public static Ranking readFromFile(Ranking lr, String file, ChiseReader cr) {
		lr.ideogramInfo = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {

			String line = null;
			while ((line = br.readLine()) != null) {
				String fields[] = line.split("\t");
				if (fields.length < 1) {
					continue;
				}
				if (fields[0].equals("character")) {
					continue;
				}

				Ideogram ig = cr.parseIdeogram(fields[0]);
				lr.add(ig);

				lr.ideogramInfo.put(ig, line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return lr;
	}

}