package dev.jh.mathquiz.config;

import java.util.LinkedList;

public record QuizConfig(
  Integer amount,
  LinkedList<Integer> equationOrder
) {
  public static final String AMOUNT = "quiz.amount";
  public static final String EQUATION_ORDER = "quiz.equation-order";
}
