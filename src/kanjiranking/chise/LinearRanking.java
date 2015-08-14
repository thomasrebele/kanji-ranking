package kanjiranking.chise;

public class LinearRanking {
	public LearningRanking learningList(String kanji, ChiseReader cr) {
		LearningRanking lr = new LearningRanking();

		for(int i=0; i<kanji.length(); i++) {
			Ideogram ig = cr.ideogram("" + kanji.charAt(i), null, -1);
			System.out.println("adding components of " + ig);
			addRecursive(lr, ig, cr);
		}
		
		return lr;
	}
	
	public void addRecursive(LearningRanking lr, Ideogram ig, ChiseReader cr) {
		if(lr.contained.contains(ig)) return;
		if(ig.components != null) {
			for(Ideogram comp : ig.components) {
				addRecursive(lr, comp, cr);
			}
		}
		lr.add(ig);
	}
}