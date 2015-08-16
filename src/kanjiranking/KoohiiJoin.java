package kanjiranking;

import java.io.IOException;
import java.io.OutputStreamWriter;

import com.opencsv.CSVWriter;

import kanjiranking.KoohiiReader.KoohiiEntry;
import kanjiranking.chise.Ideogram;
import kanjiranking.chise.LearningRanking;

public class KoohiiJoin {

	public static void main(String... args) {

		String koohiiPath = "/home/tr/Studium/sonstiges/languages/jap/software/kanji-ranking/data/kanji.koohii.com/";
		String rankingFile = "/home/tr/Studium/sonstiges/languages/jap/software/kanji-ranking/data/results/conan-order-with-wiki-frequency.txt";
		KoohiiReader kr = KoohiiReader.readFromFile(koohiiPath + "my_stories.csv");
		KoohiiReader.readFromFile(kr, koohiiPath + "my_stories_add.csv");

		LearningRanking lr = LearningRanking.readFromFile(rankingFile);

		int idx = 0;
		try {
			CSVWriter writer = new CSVWriter(new OutputStreamWriter(System.out));
			CSVWriter leftoverWriter = new CSVWriter(new OutputStreamWriter(System.out));
			String[] header = { "character", "index", "primitive", "keyword", "story", "notes" };
			writer.writeNext(header);
			leftoverWriter.writeNext(header);
			for (Ideogram ig : lr.list) {
				KoohiiEntry ke = kr.map.get(ig.toString());

				String[] fields = new String[6];
				if (ke != null) {
					fields[0] = ke.character;
					fields[1] = "" + (idx++);
					fields[2] = ke.primitive;
					fields[3] = ke.keyword;
					fields[4] = ke.story;
					fields[5] = "heisig-index: " + ke.index;

					writer.writeNext(fields);
				} else {
					fields[0] = ig.toString();
					leftoverWriter.writeNext(fields);
				}

				writer.flush();
				leftoverWriter.flush();
			}
			writer.close();
			leftoverWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
