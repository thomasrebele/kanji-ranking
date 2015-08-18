package kanjiranking;

import java.io.IOException;
import java.io.OutputStreamWriter;

import com.opencsv.CSVWriter;

import kanjiranking.KoohiiReader.KoohiiEntry;
import kanjiranking.chise.ChiseReader;
import kanjiranking.chise.Ideogram;
import kanjiranking.chise.Ranking;

public class KoohiiJoin {

	@SuppressWarnings("unused")
	public static void main(String... args) {

		String koohiiPath = "/home/tr/Studium/sonstiges/languages/jap/software/kanji-ranking/data/kanji.koohii.com/";
		String rankingFile = "/home/tr/Studium/sonstiges/languages/jap/software/kanji-ranking/data/results/conan-order-with-wiki-frequency.txt";
		String ignoreFile = "/home/tr/Studium/sonstiges/languages/jap/software/kanji-ranking/data/kanji.koohii.com/";
		KoohiiReader kr = new KoohiiReader();
		KoohiiReader.readFromFile(kr, koohiiPath + "my_stories.csv");
		KoohiiReader.readFromFile(kr, koohiiPath + "my_stories_add.csv");

		Ranking lr = Ranking.readFromFile(rankingFile);
		Ranking ignore = Ranking.readFromFile(ignoreFile + "ignore.txt");
		Ranking.readFromFile(ignore, ignoreFile + "ignore_permanent.txt", new ChiseReader());

		// System.out.println(kr.map.get("⿱甫寸"));

		int idx = 0;
		try {
			CSVWriter writer = new CSVWriter(new OutputStreamWriter(System.out));
			OutputStreamWriter leftoverWriter = new OutputStreamWriter(System.err);
			String[] header = { "character", "index", "keyword", "primitive", "story", "notes" };
			writer.writeNext(header);
			for (Ideogram ig : lr.list) {
				if (ignore.contained.contains(ig)) {
					continue;
				}
				KoohiiEntry ke = kr.map.get(ig.toString());

				String[] fields = new String[6];
				if (ke != null) {
					fields[0] = ke.character;
					fields[1] = "" + (idx++);
					fields[2] = ke.keyword;
					fields[3] = ke.primitive;
					fields[4] = ke.story;
					fields[5] = "heisig-index: none";
					try {
						if (("" + Integer.parseInt(ke.index)).equals(ke.index)) {
							fields[5] = "heisig-index: " + ke.index;
						}
					} catch (NumberFormatException e) {

					}

					writer.writeNext(fields);
				} else {
					if (leftoverWriter != null) {
						String line = null;
						if (lr.ideogramInfo != null) {
							line = lr.ideogramInfo.get(ig);
						}
						leftoverWriter.write(line == null ? ig.toString() : line);
						leftoverWriter.write("\n");
					}
				}

				writer.flush();
				if (leftoverWriter != null)
					leftoverWriter.flush();
			}
			writer.close();
			if (leftoverWriter != null)
				leftoverWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
