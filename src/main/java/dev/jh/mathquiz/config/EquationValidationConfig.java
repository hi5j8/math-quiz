package dev.jh.mathquiz.config;

public record EquationValidationConfig(
  Boolean selfDivision,
  Boolean selfSubtraction,
  Boolean negativeResults,
  Boolean decimalResults,
  Boolean decimalSolution
) {
  public static final String SELF_DIVISION = "equation.%d.validate.self-division";
  public static final String SELF_SUBTRACTION = "equation.%d.validate.self-subtraction";
  public static final String NEGATIVE_RESULTS = "equation.%d.validate.negative-results";
  public static final String DECIMAL_RESULTS = "equation.%d.validate.decimal-results";
  public static final String DECIMAL_SOLUTION = "equation.%d.validate.decimal-solution";
}
