package dev.jh.mathquiz.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

  private DateTimeUtil() {
  }

  public static String formattedDateTimeString(LocalDateTime dateTime) {
    return dateTime.format(DATE_TIME_FORMATTER);
  }

}
