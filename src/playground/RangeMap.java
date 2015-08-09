package playground;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import playground.PeekIterator;

public class RangeMap<V> extends AbstractMap<Character, V> 
implements Iterable<Entry<Character, V>>, Comparable<RangeMap<V>> {
	private ArrayList<V> data;
	private int start;
	private int end;
	private int size;
	
	public RangeMap(int start, int end) {
		this.start = start;
		this.end = end+1;
		this.size = 0;
		data = new ArrayList<V>(end-start+1);
		for(int i=start; i<end+1; i++) {
			data.add(null);
		}
	}

	/*public String toString() {
		return "" + start + "-" + end + ": " + super.toString();
	}*/
	
	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return getEnd();
	}
	
	private boolean isValidKey(int key) {
		if(key - start < 0) return false;
		if(key >= end) return false;
		return true;
	}
	
	@Override
	public V get(Object key) {
		if(!(key instanceof Character)) return null;
		int keyc = (Character)key;
		return isValidKey(keyc) ? data.get(keyc-start) : null;
	}
	
	@Override
	public V put(Character key, V value) {
		V oldvalue = null;
		if(isValidKey(key)) {
			oldvalue = data.get(key-start);
			size += (value != null ? 1 : 0) - (oldvalue != null ? 1 : 0);
			data.set(key-start, value);
		}
		return oldvalue;
	}
	
	public List<V> values() {
		return Collections.unmodifiableList(data);
	}
	
	@Override
	public Iterator<Entry<Character, V>> iterator() {
		return new PeekIterator<Entry<Character, V>>() {
			int idx = 0;
			int character = start;
			int length = end-start;

			@Override
			protected java.util.Map.Entry<Character, V> internalNext() throws Exception {
				while(idx < length && data.get(idx) == null) {
					character++; idx++;
				}
				if(idx >= length) return null;
				return new AbstractMap.SimpleEntry<Character, V>((char)(character++), data.get(idx++));
			}
		};
	}

	@Override
	public int size() {
		return size;
	}
	
	protected void recalculateSize() {
		size = 0;
		for(int i=0; i<data.size(); i++) {
			if(data.get(i) != null) size++;
		}
	}
	
	/** Copy a part of the src ArrayList to the dst ArrayList */
	private <W> void arrayListCopy(ArrayList<W> src, int srcstart, ArrayList<W> dst, int dststart, int length, BiFunction<W, W, W> updater) {
		for(int i=dststart, srcidx=srcstart; i<dststart + length; i++, srcidx++) {
			W value = src.get(srcidx);
			if(value == null) continue;
			if(updater != null) {
				W oldvalue = dst.get(i);
				if(oldvalue != null) {
					value = updater.apply(oldvalue, value);
				}
			}
			dst.set(i, value);
		}
	}

	/** Calculate margin, negative margin means intersection */
	public int calculateMargin(RangeMap<V> other) {
		int startInt = Math.max(this.start, other.start);
		int endInt = Math.min(this.end, other.end);
		return startInt - endInt;
	}
	
	/** Union with the other range map, giving precedence to this range map */
	public RangeMap<V> union(RangeMap<V> other) {
		int start = Math.min(this.start, other.start);
		int end = Math.max(this.end, other.end);
		RangeMap<V> result = new RangeMap<V>(start, end);
		result.start = start;

		// TODO: optimize this by copying only necessary data from other
		arrayListCopy(other.data, 0, result.data, other.start-start, other.data.size(), null);
		arrayListCopy(this.data, 0, result.data, this.start-start, this.data.size(), null);

		result.recalculateSize();
		return result;
	}

	/** Union with the other range map, using updater to combine colliding cells */
	public RangeMap<V> union(RangeMap<V> other, BiFunction<V, V, V> updater) {
		int start = Math.min(this.start, other.start);
		int end = Math.max(this.end, other.end);
		RangeMap<V> result = new RangeMap<V>(start, end);
		result.start = start;

		// TODO: optimize this by copying only necessary data from other
		arrayListCopy(other.data, 0, result.data, other.start-start, other.data.size(), null);
		arrayListCopy(this.data, 0, result.data, this.start-start, this.data.size(), (a,b) -> updater.apply(b, a));

		result.recalculateSize();
		return result;
	}

	@Override
	public Set<java.util.Map.Entry<Character, V>> entrySet() {
		return new AbstractSet<Map.Entry<Character, V>>() {
			@Override
			public Iterator<java.util.Map.Entry<Character, V>> iterator() {
				return RangeMap.this.iterator();
			}

			@Override
			public int size() {
				return RangeMap.this.size();
			}
			
		};
	}
	
	public static void main(String...args) {
		RangeMap<String> rm0 = new RangeMap<String>(70, 77);
		rm0.put('F', "test1: F");
		rm0.put('G', "test1: G");
		rm0.put('H', "test1: H");
		rm0.put('K', "test1: K");
		RangeMap<String> rm1 = new RangeMap<String>(75, 80);
		rm1.put('K', "test2: K");
		rm1.put('L', "test2: L");
		
		RangeMap<String> rm = rm0.union(rm1, (a,b) -> a+b);
		
		for(Entry<Character, String> e : rm) {
			System.out.println(e.getKey() + ": " + e.getValue());
		}
		System.out.println("size: " + rm.size());
	}

	@Override
	public int compareTo(RangeMap<V> o) {
		if(o == null) return -1;
		if(this.end <= o.start) return -1;
		if(this.start >= o.end) return 1;
		return 0;
	}
}