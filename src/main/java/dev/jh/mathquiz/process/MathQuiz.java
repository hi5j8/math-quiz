package dev.jh.mathquiz.process;

import dev.jh.mathquiz.util.type.Pair;

import java.util.LinkedList;
import java.util.List;

public record MathQuiz(
  List<Variable> variables,
  LinkedList<Pair<String, String>> equations
) {
}
