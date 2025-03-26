package dev.jh.mathquiz.config;

import java.util.List;

public record EquationVariableConfig(
  Integer amount,
  List<List<String>> combos,
  Boolean asIdentifiers
) {
  public static final String AMOUNT = "equation.%d.variables.amount";
  public static final String COMBOS = "equation.%d.variables.combos";
  public static final String AS_IDENTIFIERS = "equation.%d.variables.as-identifiers";
}
