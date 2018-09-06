package kanjiranking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

import com.opencsv.CSVWriter;

import kanjiranking.KoohiiReader.KoohiiEntry;
import kanjiranking.chise.ChiseReader;
import kanjiranking.chise.Ideogram;
import kanjiranking.chise.Ranking;

public class KoohiiJoin {

	String glyphWikiPath = "/home/tr/Studium/sonstiges/languages/jap/software/kanji-ranking/data/kanji.koohii.com/glyphs/";

	HashSet<String> doneGlyph;
	Path glyphPath;

	boolean downloadGlyphs = false;

	private Ranking lr;

	private Ranking ignore;

	private Ranking order;

	private Ranking definitions;

	private Ranking done;

	private KoohiiReader kr;

	private int idx;

	private int subidx;

	private boolean resetSubidx;

	private CSVWriter csvWriter;

	private OutputStreamWriter leftoverWriter;

	private void checkGlyph() {
		if (doneGlyph == null) {
			doneGlyph = new HashSet<>();
		}
		try {
			Files.createDirectories(Paths.get(glyphWikiPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		glyphPath = Paths.get(glyphWikiPath);
		for (File f : glyphPath.toFile().listFiles()) {
			String name = f.getName();
			if (name.endsWith(".svg")) {
				doneGlyph.add(name.substring(0, name.length() - 4));
			}
		}
	}

	public void downloadGlyph(String key) {
		if (doneGlyph == null) {
			checkGlyph();
		}
		if (key.startsWith("&")) {
			int end = key.indexOf(";");
			if (end > 0) {
				String glyphName = key.substring(1, end).toLowerCase();
				if (doneGlyph.contains(glyphName)) {
					return;
				}
				doneGlyph.add(glyphName);

				final File path = new File(glyphWikiPath, glyphName + ".svg");
				try {
					Files.createDirectories(path.toPath().getParent());
					URL url = new URL("http://glyphwiki.org/glyph/" + glyphName + ".svg");
					URLConnection conn = url.openConnection();
					try (final InputStream in = conn.getInputStream();) {
						Files.copy(in, path.toPath());
					} catch (FileNotFoundException e) {
						System.err.println("missing url for glyph " + glyphName + ": " + url);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void join() {

		done = new Ranking();
		Ranking orderDone = new Ranking();
		try {
			csvWriter = new CSVWriter(new OutputStreamWriter(System.out));
			leftoverWriter = new OutputStreamWriter(System.err);
			String[] header = { "character", "index", "keyword", "primitive", "story", "parts", "notes" };
			csvWriter.writeNext(header);

			idx = order == null ? -1 : 0;
			subidx = -1;
			resetSubidx = false;
			for (Ideogram ig : lr.list) {
				updateIndex(orderDone, ig);
				joinIdeogram(ig, false);

				csvWriter.flush();
				if (leftoverWriter != null)
					leftoverWriter.flush();
			}
			csvWriter.close();
			if (leftoverWriter != null)
				leftoverWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void joinIdeogram(Ideogram ig, boolean indirect) throws IOException {
		if (idx == 118) {
			System.out.println("test");
		}
		done.add(ig);

		for (Ideogram subig : ig.getAllComponents(false)) {
			if (!done.contained.contains(subig)) {
				joinIdeogram(subig, true);
			}
		}

		KoohiiEntry ke = kr.map.get(ig.toString());

		if (downloadGlyphs) {
			downloadGlyph(ig.getKey());
		}

		String[] fields = new String[7];
		if (ke != null) {
			fields[0] = ke.character;
			if (subidx >= 0) {
				fields[1] = "" + idx + "." + subidx;
			} else {
				fields[1] = "" + (idx);
			}
			fields[2] = ke.keyword;
			fields[3] = ke.primitive;
			fields[4] = ke.story;

			StringBuilder decomp = new StringBuilder();
			for (Ideogram subig : ig.getAllComponents(false)) {
				KoohiiEntry subke = kr.map.get(subig.toString());
				if (subke != null) {
					if (decomp.length() != 0) {
						decomp.append("; ");
					}
					decomp.append(subke.toShortString());
				}
			}
			fields[5] = decomp.toString();
			if (definitions != null && definitions.ideogramInfo != null) {
				String info = definitions.ideogramInfo.get(ig);
				if (info != null) {
					int pos = info.indexOf('\t');
					fields[5] += "<br>" + (pos >= 0 ? info.substring(pos + 1) : info);
				}
			}

			fields[6] = "heisig-index: none";
			try {
				if (("" + Integer.parseInt(ke.index)).equals(ke.index)) {
					fields[6] = "heisig-index: " + ke.index;
				}
			} catch (NumberFormatException e) {

			}

			csvWriter.writeNext(fields);
		} else if (indirect != true) {
			if (ignore.contained.contains(ig)) {
			} else if (leftoverWriter != null) {
				String line = null;
				if (lr.ideogramInfo != null) {
					line = lr.ideogramInfo.get(ig);
				}
				if (line == null) {
					line = ig.toString();
				}

				leftoverWriter.write(line == null ? ig.toString() : line);
				leftoverWriter.write("\n");
			}
		}
	}

	private void updateIndex(Ranking orderDone, Ideogram nextIdeogram) {
		if (resetSubidx) {
			subidx = -1;
			resetSubidx = false;
		}
		if (order == null) {
			idx++;
		} else {
			while (idx < order.list.size()) {
				Ideogram act = order.list.get(idx);
				if (!orderDone.contained.contains(act)) {
					break;
				}
				idx++;
			}

			if (order.list.get(idx).equals(nextIdeogram)) {
				resetSubidx = true;
			}

			subidx++;
			orderDone.add(nextIdeogram);
		}
	}

	public static void main(String... args) {
		String dataPath = "/home/tr/Studium/sonstiges/languages/jap/software/kanji-ranking/data/";

		String koohiiPath = dataPath + "/kanji.koohii.com/";
		String rankingFile = dataPath + "/results/conan-order-with-wiki-frequency.txt";
		String ignoreFile = dataPath + "/kanji.koohii.com/";

		KoohiiJoin kj = new KoohiiJoin();
		kj.kr = new KoohiiReader();
		KoohiiReader.readFromFile(kj.kr, koohiiPath + "my_stories.csv");
		KoohiiReader.readFromFile(kj.kr, koohiiPath + "my_stories_add.csv");

		ChiseReader cr = Main.loadChiseReader();

		kj.lr = new Ranking();
		Ranking.readFromFile(kj.lr, rankingFile, cr);
		kj.ignore = new Ranking();
		Ranking.readFromFile(kj.ignore, ignoreFile + "ignore.txt", cr);
		Ranking.readFromFile(kj.ignore, ignoreFile + "ignore_components.txt", cr);

		kj.order = Ranking.readFromString(Data.conanKanji, cr);

		kj.definitions = Ranking.readFromFile(dataPath + "/unihan/definitions.txt");

		// System.out.println(kr.map.get("⿱甫寸"));

		kj.join();
	}

}
