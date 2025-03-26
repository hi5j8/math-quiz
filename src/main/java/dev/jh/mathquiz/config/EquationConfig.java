package dev.jh.mathquiz.config;

import dev.jh.mathquiz.process.Operand;

import java.util.List;

public record EquationConfig(
  List<Operand> operands,
  EquationVariableConfig variables,
  EquationMutationConfig mutation,
  EquationValidationConfig validate,
  EquationSolutionConfig solution
) {
  public static final String PREFIX = "equation.";
  public static final String OPERANDS = "equation.%d.operands";
}
