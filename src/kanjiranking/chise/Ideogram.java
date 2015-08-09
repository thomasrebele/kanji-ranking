package kanjiranking.chise;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class Ideogram {
	
	public enum Type {
		Undefined,
		Unicode,
		Container,
		Aggregate,
		Special
	}
	
	// store info about relation to parent element
	// ids and position are part of the key
	// ids == null or position == -1 means only this ideogram without concerning parent relationship
	String key = null;
	IDSChar ids = null;
	int position = -1;

	Type type = Type.Undefined;
	ArrayList<Ideogram> parents = new ArrayList<>();
	
	String keyDesc = null;
	String decomposition = null;
	ArrayList<String> alternativeDecompositions = null;
	
	public String toString() {
		String result = key + (ids == null ? "" : "@" + ids.c + position);
		//result += " " + keyDesc + " comp: " + decomposition;
		return result;
	}
	ArrayList<Ideogram> components = null;
	
	public String decompositionToString() {
		
		String result = "";
		if(ids == null) result += "(";
		result += key + (ids == null ? "" : "@" + ids.c + position);
		
		if(components != null) {
			result += "[";
			for(int i=0; i<components.size(); i++) {
				Ideogram subig = components.get(i);
				result += subig == null ? "null" : subig.decompositionToString();
			}
			result += "]";
		}
		if(ids == null) result += ")";
		
		return result;
	}
	
	public int hashCode() {
		return key == null ? 0 : key.hashCode();
	}
	
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof Ideogram)) return false;
		Ideogram other = (Ideogram)o;
		
		boolean keyEq = key == null ? other.key == null : key.equals(other.key);
		boolean idsEq = ids == null ? other.ids == null : ids.equals(other.ids);
		boolean posEq = position < 0 ? other.position < 0 : position == other.position;
		return keyEq && idsEq && posEq;
	}
	
	public Collection<Ideogram> getAllComponents() {
		ArrayList<Ideogram> result = new ArrayList<>();
		getComponentsRecursive(result);
		return result;
	}
	
	public void getComponentsRecursive(ArrayList<Ideogram> list) {
		if(list.contains(this)) return;
		list.add(this);
		if(components != null) {
			for(Ideogram subig : components) {
				subig.getComponentsRecursive(list);
			}
		}
	}
	
	public Collection<Ideogram> getAllParents() {
		ArrayList<Ideogram> result = new ArrayList<>();
		getAllParentsRecursive(result);
		return result;
	}

	public void getAllParentsRecursive(ArrayList<Ideogram> list) {
		if(list.contains(this)) return;
		list.add(this);
		if(parents != null) {
			for(Ideogram parent : parents) {
				parent.getAllParentsRecursive(list);
			}
		}
	}
	
	public Collection<Ideogram> getAllParents(Type type) {
		return getAllParents().stream().filter(i -> i.type == Type.Unicode).collect(Collectors.toList());
	}
	
	public void addComponent(Ideogram ig) {
		if(ig == null) return;
		if(this.equals(ig)) return;
		
		components.add(ig);
		ig.parents.add(this);
	}
}