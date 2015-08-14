package kanjiranking.chise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DAG<T> {

	public class DAGNode {
		T data;
		ArrayList<T> inLinks = new ArrayList<>();
		ArrayList<T> outLinks = new ArrayList<>();
	}
	
	HashMap<T, DAGNode> dagnodes = new HashMap<>();
	ArrayList<HashSet<T>> pq = new ArrayList<>();
	
	public DAGNode node(T data) {
		DAGNode n = dagnodes.get(data);
		if(n == null) {
			n = new DAGNode();
			n.data = data;
			dagnodes.put(data, n);
			
			getPQSet(0).add(data);
		}
		return n;
	}
	
	public HashSet<T> getPQSet(int index) {
		while(pq.size() <= index) {
			pq.add(new HashSet<T>());
		}
		return pq.get(index);
	}
	
	private void pqshift(DAGNode node, int shift) {
		HashSet<T> pqset = getPQSet(node.inLinks.size());
		
		if(pqset != null) {
			pqset.remove(node.data);
		}
		
		pqset = getPQSet(node.inLinks.size()+shift);
		if(pqset != null) {
			pqset.add(node.data);
		}
	}
	
	public void addLink(T src, T dst) {
		DAGNode srcnode = node(src);
		DAGNode dstnode = node(dst);
		
		pqshift(dstnode, 1);
		
		srcnode.outLinks.add(dst);
		dstnode.inLinks.add(src);
	}
	
	public void removeLink(T src, T dst) {
		DAGNode srcnode = node(src);
		DAGNode dstnode = node(dst);
		
		pqshift(dstnode, -1);
		
		srcnode.outLinks.remove(dst);
		dstnode.inLinks.remove(src);
	}
	
	public T retrieve() {
		return retrieve(true);
	}
	
	public T retrieve(boolean onlyZeroPQ) {
		HashSet<T> pqset = null;
		int until = onlyZeroPQ ? 1 : pq.size();
		for(int i=0; i<until; i++) {
			pqset = pq.get(i);
			if(pq.get(i) != null && pqset.size() > 0) {
				break;
			}
		}
		if(pqset == null) return null;
		if(!pqset.iterator().hasNext()) {
			return null;
		}
		
		T result = pqset.iterator().next();
		
		DAGNode n = node(result);
		ArrayList<T> outLinks = new ArrayList<>(n.outLinks);
		for(T outLink : outLinks) {
			removeLink(n.data, outLink);
		}
		
		pqset.remove(result);
		return result;
	}
	
	public static void main(String...args) {
		DAG<String> dag = new DAG<String>();
		
		dag.addLink("1", "1.1");
		dag.addLink("2", "1.1");
		dag.addLink("1.1", "3");
		dag.addLink("3", "4");
		System.out.println(dag.pq);
		
		for(int i=0; i<7; i++) {
			System.out.println(dag.retrieve());
		}
	}
}
