package kanjiranking.chise;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Ideogram {

	public enum Type {
		Undefined, Unicode, Container, Aggregate, Special
	}

	// store info about relation to parent element
	// ids and position are part of the key
	// ids == null or position == -1 means only this ideogram without concerning
	// parent relationship
	String key = null;
	IDSChar ids = null;
	int position = -1;

	Type type = Type.Undefined;
	ArrayList<Ideogram> parents = new ArrayList<>();

	String keyDesc = null;
	String decomposition = null;
	ArrayList<String> alternativeDecompositions = null;

	ArrayList<Ideogram> components = null;

	@Override
	public String toString() {
		if (ids == null)
			return key;

		StringBuilder result = new StringBuilder();
		result.append(ids.c);
		for (int i = 0; i < ids.argCount; i++) {
			if (i == position) {
				result.append(key);
			} else {
				result.append("_");
			}
		}
		return result.toString();
	}

	public String decompositionToString() {

		String result = "";
		if (ids == null)
			result += "(";
		result += key + (ids == null ? "" : "@" + ids.c + position);

		if (components != null) {
			result += "[";
			for (int i = 0; i < components.size(); i++) {
				Ideogram subig = components.get(i);
				result += subig == null ? "null" : subig.decompositionToString();
			}
			result += "]";
		}
		if (ids == null)
			result += ")";

		return result;
	}

	@Override
	public int hashCode() {
		return key == null ? 0 : key.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof Ideogram))
			return false;
		Ideogram other = (Ideogram) o;

		boolean keyEq = key == null ? other.key == null : key.equals(other.key);
		boolean idsEq = ids == null ? other.ids == null : ids.equals(other.ids);
		boolean posEq = position < 0 ? other.position < 0 : position == other.position;
		return keyEq && idsEq && posEq;
	}

	public String getKey() {
		return key;
	}

	public Type getType() {
		return type;
	}

	public Collection<Ideogram> getAllComponents() {
		return getAllComponents(true);
	}

	public Collection<Ideogram> getAllComponents(boolean includeFirst) {
		ArrayList<Ideogram> result = new ArrayList<>();
		getComponentsRecursive(result, includeFirst);
		return result;
	}

	public void getComponentsRecursive(ArrayList<Ideogram> list, boolean includeFirst) {
		if (list.contains(this))
			return;
		if (includeFirst)
			list.add(this);
		if (components != null) {
			for (Ideogram subig : components) {
				subig.getComponentsRecursive(list, true);
			}
		}
	}

	public List<Ideogram> getParents(int maxRecursionDepth) {
		ArrayList<Ideogram> result = new ArrayList<>();
		getAllParentsRecursive(result, maxRecursionDepth);
		return result;
	}

	public void getAllParentsRecursive(ArrayList<Ideogram> list, int maxRecursionDepth) {
		if (maxRecursionDepth < 0)
			return;
		if (list.contains(this))
			return;
		list.add(this);
		if (parents != null) {
			for (Ideogram parent : parents) {
				parent.getAllParentsRecursive(list, maxRecursionDepth - 1);
			}
		}
	}

	public List<Ideogram> getParents() {
		return parents;
	}

	public List<Ideogram> getAllParents(Type type) {
		return getAllParents(type, Integer.MAX_VALUE);
	}

	public List<Ideogram> getAllParents(Type type, int maxRecursionDepth) {
		return getParents(maxRecursionDepth).stream().filter(i -> i.type == Type.Unicode).collect(Collectors.toList());
	}

	public Collection<Ideogram> getAllParents() {
		return getParents(Integer.MAX_VALUE);
	}

	public void addComponent(Ideogram ig) {
		if (ig == null)
			return;
		if (this.equals(ig))
			return;
		if (components == null) {
			components = new ArrayList<>();
		}

		components.add(ig);
		ig.parents.add(this);
	}
}