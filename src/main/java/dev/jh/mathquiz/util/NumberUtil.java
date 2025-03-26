package dev.jh.mathquiz.util;

import dev.jh.mathquiz.util.type.Range;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

public final class NumberUtil {

  private NumberUtil() {
  }

  public static int randomInt(int min, int max) {
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }

  public static boolean isWithinRange(Range range, BigDecimal number) {
    return number.compareTo(range.min()) >= 0 && number.compareTo(range.max()) <= 0;
  }

}
