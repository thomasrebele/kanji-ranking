package playground;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import playground.PeekIterator;

public class MultiRangeMap<V> extends AbstractMap<Character, V> {

  protected ArrayList<RangeMap<V>> data;

  int newSubRMCounter = 0;

  final int mergeRangeMapDistance = 5;

  public MultiRangeMap() {
    data = new ArrayList<>();
    fixDataConstraints();
  }

  /** Assure that list is sorted and try to merge range maps */
  protected void fixDataConstraints() {
    data.sort(null);
    ArrayList<RangeMap<V>> newData = new ArrayList<RangeMap<V>>();
    RangeMap<V> act = null;
    for (int i = 0; i < data.size(); i++) {
      RangeMap<V> other = data.get(i);
      if (other == null) continue;
      if (act == null) {
        act = data.get(i);
        continue;
      }
      int margin = act.calculateMargin(other);
      if (margin < mergeRangeMapDistance) {
        act = act.union(other);
      } else {
        newData.add(act);
        act = other;
      }
    }
    if (act != null) newData.add(act);
    data = newData;
  }

  /** Search corresponding range map in data array, or create it if 'create' is set */
  protected RangeMap<V> getRangeMap(Character c, boolean create) {
    // TODO: insert new range map at -index-1, avoids sorting of the array
    int index = Collections.binarySearch(data, new RangeMap<V>(c, c + 1));
    if (index >= 0) return data.get(index);
    if (create) {
      RangeMap<V> rm = new RangeMap<V>(c, c + 1);
      data.add(rm);
      fixDataConstraints();
      return getRangeMap(c, create);
    }
    return null;
  }

  @Override
  public V get(Object c) {
    if (!(c instanceof Character)) return null;
    RangeMap<V> rm = getRangeMap((Character) c, false);
    if (rm != null) return rm.get(c);
    return null;
  }

  @Override
  public V put(Character c, V value) {
    RangeMap<V> submap = getRangeMap(c, true);
    V oldvalue = null;
    if (submap != null) {
      oldvalue = submap.put(c, value);
    }

    return oldvalue;
  }

  @Override
  public Set<java.util.Map.Entry<Character, V>> entrySet() {
    return new AbstractSet<Map.Entry<Character, V>>() {

      @Override
      public Iterator<java.util.Map.Entry<Character, V>> iterator() {
        return new PeekIterator<Map.Entry<Character, V>>() {

          int pos = 0;

          Iterator<java.util.Map.Entry<Character, V>> it = null;

          @Override
          protected java.util.Map.Entry<Character, V> internalNext() throws Exception {
            while (pos < data.size() && (it == null || !it.hasNext())) {
              RangeMap<V> submap = data.get(pos);
              it = submap == null ? null : submap.iterator();
              pos++;
            }
            if (it != null && it.hasNext()) return it.next();
            return null;
          }
        };
      }

      @Override
      public int size() {
        int size = 0;
        for (RangeMap<V> submap : data) {
          if (submap != null) {
            size += submap.size();
          }
        }
        return size;
      }
    };
  }

  public static void main(String[] args) {
    MultiRangeMap<String> mrm = new MultiRangeMap<String>();
    for (char c = 'a'; c < 'z'; c++) {
      mrm.put(c, "<" + c + ">");
    }
    mrm.put('F', "<F>");
    mrm.put('X', "<X>");
    mrm.put('D', "<D>");
    mrm.put('G', "<G>");
    mrm.put('Z', "<Z>");
    mrm.put('[', "<[>");
    mrm.put('.', "<.>");

    System.out.println(mrm.toString());

    System.out.println(mrm.data);
  }

}
