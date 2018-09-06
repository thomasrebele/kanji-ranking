package kanjiranking;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kanjiranking.CharCount.CharStat;
import kanjiranking.chise.ChiseReader;
import kanjiranking.chise.Ideogram;
import kanjiranking.chise.Ideogram.Type;
import kanjiranking.chise.LinearRanking;
import kanjiranking.chise.Ranking;

public class Main {

	public static String readAll(String path) {
		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
			return new String(encoded);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ChiseReader loadChiseReader() {
		String dataPath = "/home/tr/Studium/sonstiges/languages/jap/software/kanji-ranking/data/";
		Path p = Paths.get(dataPath + "chise");
		ChiseReader cr = new ChiseReader();

		File[] files = p.toFile().listFiles();
		for (File f : files) {
			if (f.getName().equals("IDS-UCS-Basic.txt")) {
				// if (f.getName().equals("test.txt")) {
				cr.readFile(f);
				// System.out.println(f);
			}
		}

		cr.readFile(new File(dataPath + "/kanji.koohii.com/userdefined_decomposition.txt"));
		return cr;
	}

	public static void main(String... args) {
		String dataPath = "/home/tr/Studium/sonstiges/languages/jap/software/kanji-ranking/data/";
		ChiseReader cr = loadChiseReader();

		/*{
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					line = line.trim();
					Ideogram ig = cr.ideogram(line);
					if (ig == null) {
						System.out.println("ideogram '" + line + "' not found");
					} else {
						System.out.println(ig.getAllComponents());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/

		String order = "";
		order = readAll(dataPath + "heisig/heisig-all.txt");
		order = Data.conanKanji.substring(0);

		Ranking lr = new LinearRanking().learningList(order, cr);

		CharCount csKanji = CharCount.readFromFile(Paths.get(dataPath + "wiki/wikipedia-kanji-frequency.txt").toFile());
		HashMap<Ideogram, CharStat> compStatsKanji = calculateComponentStat(cr, csKanji);

		CharCount csHanzi = CharCount.readFromFile(Paths.get(dataPath + "wiki/wikipedia-hanzi-frequency.txt").toFile());
		HashMap<Ideogram, CharStat> compStatsHanzi = calculateComponentStat(cr, csHanzi);

		Ranking definitions = Ranking.readFromFile(dataPath + "/unihan/definitions.txt");

		OutputStreamWriter normal = new OutputStreamWriter(System.out);
		OutputStreamWriter combined = null;
		/*try {
			combined = new OutputStreamWriter(
					new FileOutputStream(dataPath + "kanji.koohii.com/ignore_components.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/

		System.out.println("character\tkanji_frequency\tparent_count\thanzi_frequency\tparent_count");
		DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		df.setMaximumFractionDigits(6);
		df.setMinimumFractionDigits(6);
		for (Ideogram ig : lr.list) {

			boolean skip = false;
			skip |= isSkipIdeogram(cr, compStatsKanji, ig);

			if (skip) {
				continue;
			}

			StringBuilder line = new StringBuilder();
			String c = ig.toString();
			line.append(c);
			line.append("\tidx:" + (order.indexOf(c) + 1));
			appendStats(compStatsKanji, df, ig, line);
			appendStats(compStatsHanzi, df, ig, line);

			if (ig.getType() == Type.Container) {
				Ideogram igPure = cr.ideogram(ig.getKey());
				line.append("\t");
				line.append(igPure.toString());
				appendStats(compStatsKanji, df, igPure, line);
				appendStats(compStatsHanzi, df, igPure, line);
			} else if (ig.getType() == Type.Unicode || ig.getType() == Type.Special) {
				line.append("\t");
				line.append(ig.getAllComponents(false));
				line.append("\t");
				List<Ideogram> parents = ig.getAllParents(Type.Unicode, 3);
				if (parents.size() > 10) {
					parents = parents.subList(0, 10);
				}
				line.append(parents);
			}

			if (ig.getType() == Type.Unicode) {
				if (definitions.ideogramInfo != null) {
					String info = definitions.ideogramInfo.get(ig);
					if (info != null) {
						int pos = info.indexOf('\t');
						line.append("\t" + (pos >= 0 ? info.substring(pos + 1) : info));
					}
				}

			}

			if (ig.getType() == Type.Aggregate || ig.getType() == Type.Container) {
				if (combined != null) {
					try {
						combined.write(line + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				System.out.println(line);
			}
		}

		//
	}

	private static void appendStats(HashMap<Ideogram, CharStat> compStatsKanji, DecimalFormat df, Ideogram ig,
			StringBuilder line) {
		CharStat cs = compStatsKanji.get(ig);
		if (cs != null) {
			line.append("\t");
			line.append(df.format(cs.getFrequency()));
			line.append("%\t");
			line.append(String.format("%1$4s", cs.other));
		}
	}

	private static boolean isSkipIdeogram(ChiseReader cr, HashMap<Ideogram, CharStat> compStatsKanji, Ideogram ig) {
		if (ig == null)
			return true;
		boolean skip = false;
		List<Ideogram> parents = ig.getParents();
		if (ig.getType() == Type.Container) {
			Ideogram igPure = cr.ideogram(ig.getKey());
			CharStat cs = compStatsKanji.get(ig);
			CharStat csPure = compStatsKanji.get(igPure);
			if (csPure != null) {
				double ratio = (double) cs.getCount() / csPure.getCount();
				// System.out.println(cs + " " + csPure + " ratio " +
				// ratio);
				/*if (csPure.getCount() < 5 || ratio < 0.15 || (1 - ratio) < 0.15)
					skip = true;*/

				/*if (ratio == 1)
					skip = false;*/
			}
		}

		if (!skip && parents.size() == 1 && (ig.getType() == Type.Aggregate || ig.getType() == Type.Container)) {
			skip = true;
		}
		return skip;
	}

	private static HashMap<Ideogram, CharStat> calculateComponentStat(ChiseReader cr, CharCount csKanji) {
		HashMap<Ideogram, CharStat> compStats = new HashMap<Ideogram, CharStat>();
		for (CharStat cstat : csKanji.mrm.values()) {
			String c = cstat.getChar();
			Ideogram i = cr.ideogram(c, null, -1);
			for (Ideogram comp : i.getAllComponents()) {
				CharStat compStat = compStats.compute(comp,
						(k, v) -> v == null ? csKanji.new CharStat(k.toString()) : v);
				compStat.add(cstat);
				compStat.other += 1;
			}
		}
		return compStats;
	}

}
