package dev.jh.mathquiz.config;

import dev.jh.mathquiz.util.type.Range;

public record VariableConfig(
  String identifier,
  Range range
) {
  public static final String PREFIX = "variable.";
  public static final String IDENTIFIER = "variable.%d.identifier";
  public static final String RANGE = "variable.%d.range";
}
