package kanjiranking.chise;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class LearningRanking {
	public HashSet<Ideogram> contained = new HashSet<>();
	public ArrayList<Ideogram> list = new ArrayList<>();

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

	public static LearningRanking readFromFile(String file) {
		return readFromFile(file, new ChiseReader());
	}

	public static LearningRanking readFromFile(String file, ChiseReader cr) {
		LearningRanking lr = new LearningRanking();
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
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return lr;
	}

}