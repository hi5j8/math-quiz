package dev.jh.mathquiz.validate.expression;

import dev.jh.mathquiz.process.Operand;

import java.util.LinkedList;

public record ExpressionNode(
  String value,
  Operand operand,
  LinkedList<ExpressionNode> nodes
) {
  public boolean hasNodes() {
    return nodes != null && !nodes.isEmpty();
  }
}
