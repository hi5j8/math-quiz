package dev.jh.mathquiz.config;

import dev.jh.mathquiz.util.type.Range;

import java.math.BigDecimal;
import java.util.List;

public record EquationMutationConfig(
  Boolean enabled,
  BigDecimal chance,
  BigDecimal multiplier,
  Range amplifier,
  List<String> affectedVariables
) {
  public static final String ENABLED = "equation.%d.mutation.enabled";
  public static final String CHANCE = "equation.%d.mutation.chance";
  public static final String MULTIPLIER = "equation.%d.mutation.multiplier";
  public static final String AMPLIFIER = "equation.%d.mutation.amplifier";
  public static final String AFFECTED_VARIABLES = "equation.%d.mutation.affected-variables";
}
