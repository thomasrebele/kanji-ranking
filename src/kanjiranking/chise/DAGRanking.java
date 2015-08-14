package kanjiranking.chise;

import java.util.Collection;

import kanjiranking.chise.ChiseReader.IdeogramCount;
import kanjiranking.chise.ChiseReader.Stats;

public class DAGRanking {
	public LearningRanking learningList(Stats stats) {
		LearningRanking lr = new LearningRanking();
		DAG<Ideogram> dag = new DAG<>();

		int i=0;
		for(IdeogramCount igc : stats.componentsByOccurences) {
			Ideogram ig = igc.i;
			if(lr.contained.contains(ig)) continue;
			if(ig.components == null || ig.getAllComponents(false).size() == 0) {
				// add parents
				Collection<Ideogram> parents = ig.getAllParents();
				for(Ideogram parent : parents) {
					if(stats.componentCount.get(parent) == null) continue;
					dag.addLink(ig, parent);
				}
			}
		}

		while(true) {
			Ideogram next = dag.retrieve(false);
			if(next == null) {
				break;
			}
			if(i++<1000) {
				System.out.println(next);
			}
			lr.add(next);
		}

		return lr;
	}
}