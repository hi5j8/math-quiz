package dev.jh.mathquiz.util;

import dev.jh.mathquiz.util.type.Range;

import java.math.BigDecimal;
import java.util.*;

public final class PropertiesUtil {

  private PropertiesUtil() {
  }

  public static  Map<Integer, Properties> toPropertiesPerIndexMap(Properties properties, String propertyPrefix) {
    Map<Integer, Properties> result = new HashMap<>();

    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      String key = String.valueOf(entry.getKey());

      if (!String.valueOf(key).startsWith(propertyPrefix)) {
        continue;
      }

      Integer index;
      try {
        index = Integer.parseInt(String.valueOf(
          key.charAt(propertyPrefix.length())
        ));
      } catch (NumberFormatException e) {
        continue;
      }

      if (!result.containsKey(index) || result.get(index).isEmpty()) {
        Properties indexProperties = new Properties();
        indexProperties.put(entry.getKey(), entry.getValue());
        result.put(index, indexProperties);
      } else {
        result.get(index).put(entry.getKey(), entry.getValue());
      }
    }

    return result;
  }

  public static List<String> getStringList(
    Properties properties, String key, String delimiter, List<String> fallback
  ) {
    try {
      String value = properties.getProperty(key);
      String[] split = value.split(delimiter);
      return Arrays.asList(split);
    } catch (NullPointerException e) {
      return fallback;
    }
  }

  public static Integer getInteger(Properties properties, String key, Integer fallback) {
    try {
      return Integer.parseInt(properties.getProperty(key));
    } catch (NullPointerException | NumberFormatException e) {
      return fallback;
    }
  }

  public static LinkedList<Integer> getIntegerLinkedList(
    Properties properties, String key, String delimiter, LinkedList<Integer> fallback
  ) {
    try {
      String value = properties.getProperty(key);
      String[] split = value.split(delimiter);
      LinkedList<Integer> result = new LinkedList<>();
      for (String s : split) {
        result.add(Integer.parseInt(s));
      }
      return result;
    } catch (NullPointerException | NumberFormatException e) {
      return fallback;
    }
  }

  public static Range getRange(Properties properties, String key, String delimiter, Range fallback) {
    try {
      String value = properties.getProperty(key);
      String[] split = value.split(delimiter);
      return new Range(new BigDecimal(split[0]), new BigDecimal(split[1]));
    } catch (NullPointerException | NumberFormatException e) {
      return fallback;
    }
  }

  public static List<List<String>> getStringListList(
    Properties properties, String key, String entryDelimiter, String valueDelimiter, List<List<String>> fallback
  ) {
    try {
      String value = properties.getProperty(key);
      String[] split = value.split(entryDelimiter);
      List<List<String>> result = new ArrayList<>();
      for (String entry : split) {
        String[] entrySplit = entry.split(valueDelimiter);
        List<String> entryList = Arrays.asList(entrySplit);
        if (!entryList.isEmpty()) {
          result.add(entryList);
        }
      }
      return result.isEmpty() ? null : result;
    } catch (Exception e) {
      return null;
    }
  }

  public static Boolean getBoolean(Properties properties, String key, Boolean fallback) {
    try {
      return Boolean.parseBoolean(properties.getProperty(key));
    } catch (Exception e) {
      return fallback;
    }
  }

  public static BigDecimal getBigDecimal(Properties properties, String key, BigDecimal fallback) {
    try {
      return new BigDecimal(properties.getProperty(key));
    } catch (NullPointerException | NumberFormatException e) {
      return fallback;
    }
  }
}
