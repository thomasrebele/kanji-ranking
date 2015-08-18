package kanjiranking.chise;

public class LinearRanking {
	public Ranking learningList(String kanji, ChiseReader cr) {
		Ranking lr = new Ranking();

		for (int i = 0; i < kanji.length(); i++) {
			Ideogram ig = cr.ideogram("" + kanji.charAt(i), null, -1);
			addRecursive(lr, ig, cr);
		}

		return lr;
	}

	public void addRecursive(Ranking lr, Ideogram ig, ChiseReader cr) {
		if (lr.contained.contains(ig))
			return;
		if (ig.components != null) {
			for (Ideogram comp : ig.components) {
				addRecursive(lr, comp, cr);
			}
		}
		lr.add(ig);
	}
}