package dev.jh.mathquiz.util;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class CollectionUtil {

  private CollectionUtil() {
  }

  public static <K, V> NavigableMap<K, V> toKeySortedMap(Map<K, V> map, boolean ascending) {
    TreeMap<K, V> inputAsTreeMap = new TreeMap<>(map);
    TreeMap<K, V> sorted = new TreeMap<>(inputAsTreeMap.descendingMap());
    return ascending ? sorted.reversed() : sorted;
  }

  public static boolean hasContent(List<?> l) {
    return l != null && !l.isEmpty();
  }

  public static boolean hasNoContent(List<?> l) {
    return l == null || l.isEmpty();
  }

}
