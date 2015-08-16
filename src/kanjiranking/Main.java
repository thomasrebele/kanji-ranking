package kanjiranking;

import java.io.File;
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
import kanjiranking.chise.LearningRanking;
import kanjiranking.chise.LinearRanking;

public class Main {
	public static void main(String... args) {
		String dataPath = "/home/tr/Studium/sonstiges/languages/jap/software/kanji-ranking/data/";
		Path p = Paths.get(dataPath + "chise");
		ChiseReader cr = new ChiseReader();

		File[] files = p.toFile().listFiles();
		for (File f : files) {
			if (f.getName().equals("IDS-UCS-Basic.txt") || f.getName().equals("userdefined.txt")) {
				// if (f.getName().equals("test.txt")) {
				cr.readFile(f);
				// System.out.println(f);
			}
		}
		// list of components
		/*
		 * for(Entry<Ideogram, Integer> ig : componentCount.entrySet()) {
		 * System.out.println(ig); }
		 */

		/*
		 * Ideogram ig = cr.ideogram("鼻", null, -1);
		 * System.out.println(ig.getAllComponents()); Ideogram ig2 =
		 * cr.ideogram("木", null, -1);
		 * System.out.println(ig2.getAllComponents());
		 */

		// Stats st200 = cr.getStatistics(Data.conanKanji.substring(0, 200));
		// Stats stall = cr.getStatistics(Data.conanKanji);
		// LearningRanking lr = new
		// LinearRanking().learningList(Data.conanKanji.substring(0,200), cr);
		LearningRanking lr = new LinearRanking().learningList(Data.conanKanji.substring(0), cr);
		// System.out.println(lr);
		// System.out.println(cr.learningList(stall));

		CharCount csKanji = CharCount.readFromFile(Paths.get(dataPath + "wiki/wikipedia-kanji-frequency.txt").toFile());
		HashMap<Ideogram, CharStat> compStatsKanji = calculateComponentStat(cr, csKanji);

		/*Ideogram ig = cr.ideogram("可", cr.idschar("⿰"), 1);
		System.out.println(ig);
		{
			CharStat compStat = compStatsKanji.get(ig);
			System.out.println(compStat + " char count " + (compStat == null ? "" : compStat.other));
		}*/

		CharCount csHanzi = CharCount.readFromFile(Paths.get(dataPath + "wiki/wikipedia-hanzi-frequency.txt").toFile());
		HashMap<Ideogram, CharStat> compStatsHanzi = calculateComponentStat(cr, csHanzi);

		/*System.out.println(ig);
		{
			CharStat compStat = compStatsHanzi.get(ig);
			System.out.println(compStat + " char count " + (compStat == null ? "" : compStat.other));
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
			appendStats(compStatsKanji, df, ig, line);
			appendStats(compStatsHanzi, df, ig, line);

			if (ig.getType() == Type.Container) {
				Ideogram igPure = cr.ideogram(ig.getKey());
				line.append("\t");
				line.append(igPure.toString());
				appendStats(compStatsKanji, df, igPure, line);
				appendStats(compStatsHanzi, df, igPure, line);
			}

			System.out.println(line);
		}

		// System.out.println(compStatsKanji.get(cr.parseIdeogram("⿸_奄")));
		// System.out.println(compStatsKanji.get(cr.parseIdeogram("⿳𠂉__")));
		// special components
		/*
		 * for(Ideogram special : specials) { Collection<Ideogram> parents =
		 * special.getAllParents(Type.Unicode); if(parents.isEmpty()) { parents
		 * = special.getAllParents(); } System.out.println(special + " " +
		 * parents); } System.out.println("special characters " +
		 * specials.size());
		 */

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
				if (csPure.getCount() < 5 || ratio < 0.15 || (1 - ratio) < 0.15)
					skip = true;

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
