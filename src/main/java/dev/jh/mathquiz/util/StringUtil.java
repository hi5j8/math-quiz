package dev.jh.mathquiz.util;

import dev.jh.mathquiz.util.type.Range;

import java.util.Iterator;
import java.util.List;

public final class StringUtil {

  private StringUtil() {
  }

  public static boolean hasContent(String s) {
    return s != null && !s.isEmpty();
  }

  public static boolean hasNoContent(String s) {
    return !hasContent(s);
  }

  public static boolean allHaveContent(String... strings) {
    for (String s : strings) {
      if (hasNoContent(s)) {
        return false;
      }
    }
    return true;
  }

  public static String concat(Range range, String delimiter) {
    if (range == null || range.min() == null || range.max() == null || hasNoContent(delimiter)) {
      return null;
    }
    return concat(List.of(range.min(), range.max()), delimiter);
  }

  public static String concat(List<?> list, String delimiter) {
    if (CollectionUtil.hasNoContent(list) || hasNoContent(delimiter)) {
      return null;
    }
    StringBuilder resultBuilder = new StringBuilder();
    Iterator<?> iterator = list.iterator();
    while (iterator.hasNext()) {
      resultBuilder.append(iterator.next().toString());
      if (iterator.hasNext()) {
        resultBuilder.append(delimiter);
      }
    }
    return resultBuilder.toString();
  }

}
