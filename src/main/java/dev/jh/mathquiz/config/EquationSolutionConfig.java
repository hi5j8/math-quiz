package dev.jh.mathquiz.config;

import dev.jh.mathquiz.util.type.Range;

public record EquationSolutionConfig(
  Range range,
  Boolean hide
) {
  public static final String RANGE = "equation.%d.solution.range";
  public static final String HIDE = "equation.%d.solution.hide";
}
