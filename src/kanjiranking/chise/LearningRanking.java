package kanjiranking.chise;

import java.util.ArrayList;
import java.util.HashSet;

public class LearningRanking {
	HashSet<Ideogram> contained = new HashSet<>();
	ArrayList<Ideogram> list = new ArrayList<>();
	
	public String toString() {
		return list.toString();
	}
	
	public void add(Ideogram ig) {
		contained.add(ig);
		list.add(ig);
	}
}