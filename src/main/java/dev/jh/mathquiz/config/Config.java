package dev.jh.mathquiz.config;

import java.util.LinkedList;

public record Config(
  ExportConfig export,
  QuizConfig quiz,
  LinkedList<VariableConfig> variables,
  LinkedList<EquationConfig> equations
) {
}
