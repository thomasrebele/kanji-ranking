package playground;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import playground.PeekIterator;

public class RangeTrieMap<V> implements Map<CharSequence, V> {
	private Map<Character, RangeTrieMap<V>> subtries;
	private V data;

	public void toString(StringBuilder builder, String prefix) {
		if(data != null) {
			builder.append(prefix);
			builder.append(": ");
			builder.append(data);
			builder.append("; ");
		}
		if(subtries == null) return;
		for(Map.Entry<Character, RangeTrieMap<V>> subtrie : subtries.entrySet()) {
			if(subtrie.getValue() != null) {
				subtrie.getValue().toString(builder, prefix + subtrie.getKey());
			}
		}
		builder.toString();
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		toString(builder, "");
		return builder.toString();
	}
	
	private V getOrPut(CharSequence key, int depth, Optional<V> value) {
		boolean doPut = value != null && value.isPresent();
		if(depth == key.length()) {
			V result = data;
			if(doPut) {
				data = value.get();
			}
			return result;
		}
		if(depth >= key.length()) return null;
		if(doPut && subtries == null) {
			subtries = new MultiRangeMap<RangeTrieMap<V>>();
		}
		RangeTrieMap<V> subtrie = subtries == null ? null : subtries.get(key.charAt(depth));
		if(subtrie == null) {
			if(doPut) {
				subtrie = new RangeTrieMap<V>();
				subtries.put(key.charAt(depth), subtrie);
				return subtrie.getOrPut(key, depth + 1, value);
			}
			else {
				return null;
			}
		}
		else {
			return subtrie.getOrPut(key, depth + 1, value);
		}
	}
	
	@Override
	public V get(Object key) {
		if(!(key instanceof String)) return null;
		return getOrPut((String)key, 0, null);
	}
	
	@Override
	public V put(CharSequence key, V value) {
		return getOrPut(key, 0, Optional.of(value));
	}

	@Override
	public int size() {
		// TODO: save size as value
		int size = 0;
		if(data != null) size++;
		if(subtries != null) {
			for(RangeTrieMap<V> subtrie : subtries.values()) {
				if(subtrie == null) continue;
				size += subtrie.size();
			}
		}
		return size;
	}

	@Override
	public boolean isEmpty() {
		// TODO: save size as value
		if(data != null) return false;
		for(RangeTrieMap<V> subtrie : subtries.values()) {
			if(subtrie == null) continue;
			if(subtrie.isEmpty()) continue;
			else return false;
		}
		return true;
	}

	@Override
	public boolean containsKey(Object key) {
		return get(key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key) {
		if(!(key instanceof String)) return null;
		return put((String)key, null);
	}

	@Override
	public void putAll(Map<? extends CharSequence, ? extends V> m) {
		for(Entry<? extends CharSequence, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void clear() {
		data = null;
		subtries = null;
	}

	@Override
	public Set<CharSequence> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	private Iterator<Entry<CharSequence, V>> iterator(final String prefix) {
		return new PeekIterator<Entry<CharSequence, V>>() {
			Iterator<Entry<Character, RangeTrieMap<V>>> stack = null;
			Iterator<Entry<CharSequence, V>> stack2 = null;
			String localPrefix;
			boolean isInitialized = false;

			@Override
			protected Entry<CharSequence, V> internalNext() throws Exception {
				try {
				if( stack == null) {
					if(!isInitialized) {
						isInitialized = true;
						if(subtries != null) {
							stack = subtries.entrySet().iterator();
						}
					}
					else {
						return null;
					}
					if(data != null) {
						return new AbstractMap.SimpleEntry<CharSequence, V>(prefix, data);
					}
				}
				if(stack == null) return null;
				
				while(stack2 == null || stack2.hasNext() == false) {
					if(!stack.hasNext()) return null;
					Entry<Character, RangeTrieMap<V>> e = stack.next();
					localPrefix = prefix + e.getKey();
					RangeTrieMap<V> subtrie = e.getValue();
					if(subtrie != null) {
						stack2 = subtrie.iterator(localPrefix);
					}
				}
				
				return stack2.next();
				}
				catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		};
	}
	
	@Override
	public Set<Entry<CharSequence, V>> entrySet() {
		return new AbstractSet<Entry<CharSequence, V>>() {
			@Override
			public Iterator<Entry<CharSequence, V>> iterator() {
				return RangeTrieMap.this.iterator("");
			}

			@Override
			public int size() {
				return RangeTrieMap.this.size();
			}
		};
	}
	
	public static void main(String args[]) {
		RangeTrieMap<String> trie = new RangeTrieMap<String>();
		
		trie.put("z", "<z>");
		trie.put("abc", "<abc>");
		trie.put("a", "<a>");
		trie.put("in", "<in>");
		trie.put("def", "<def>");
		trie.put("xyz", "<xyz>");
		trie.put("abc", "<abc2>");

		for(Entry<CharSequence, String> e : trie.entrySet()) {
			System.out.println(e);
		}
	}
}