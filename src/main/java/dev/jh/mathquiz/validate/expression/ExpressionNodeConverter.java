package dev.jh.mathquiz.validate.expression;

import dev.jh.mathquiz.process.Operand;
import dev.jh.mathquiz.util.ExpressionUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExpressionNodeConverter {

  public static final String OPEN_PARENTHESES = "(";
  public static final String CLOSE_PARANTHESES = ")";
  private static final String EQUALS = "=";

  public ExpressionNode toExpressionNode(String equation) {
    if (equation == null || equation.isBlank()) {
      return null;
    }
    String expression = equation.contains(EQUALS) ? equation.split(EQUALS)[0] : equation;
    return expression != null && !expression.isBlank() ? toExpressionNode(expression, Operand.SUBTRACTION) : null;
  }

  public ExpressionNode toExpressionNode(String expression, Operand operand) {
    if (ExpressionUtil.isIsolatedValue(expression)) {
      // leaf level has been reached -> return the node with just the expression as the value,
      // and no more sub-nodes and operand
      return new ExpressionNode(expression, null, null);
    }

    if (operand == null) {
      // expression has been split by all possible operands -> assume the remaining expression is a sub-expression,
      // wrapped in parentheses. unwrap and process it again, beginning with the first operand again
      String subExpression = expression.substring(1, expression.length() - 1);
      return toExpressionNode(subExpression, Operand.SUBTRACTION);
    }

    // the expression is not a sub-expression, and not a single value. it can still be split into more sub-nodes
    // by any of the remaining operands

    // attempt to split expression with current operand
    List<String> parts = splitExpressionBy(expression, operand);
    Operand nextOperand = getNextOperand(operand);
    LinkedList<ExpressionNode> nodes = new LinkedList<>();

    if (parts.size() == 1) {
      // if the split didn't do anything (e.g. 4/2 split by MULTIPLY), ignore it,
      // and attempt ti with the next operand
      return toExpressionNode(expression, nextOperand);
    }

    // further process each part using the next operand and assemble the final tree from bottom-up
    parts.forEach(part -> nodes.add(toExpressionNode(part, nextOperand)));
    return new ExpressionNode(expression, operand, nodes);
  }

  private Operand getNextOperand(Operand operand) {
    return switch (operand) {
      case Operand.SUBTRACTION -> Operand.ADDITION;
      case Operand.ADDITION -> Operand.MULTIPLICATION;
      case Operand.MULTIPLICATION -> Operand.DIVISION;
      case Operand.DIVISION -> null;
    };
  }

  private List<String> splitExpressionBy(String expression, Operand operand) {
    if (expression == null || expression.isBlank() || operand == null) {
      return new ArrayList<>();
    }

    List<String> result = new LinkedList<>();
    int lastOperandIndex = 0;
    int subExpressionLevel = 0;

    for (int i = 0; i < expression.length(); i++) {
      String currentSymbol = String.valueOf(expression.charAt(i));

      if (OPEN_PARENTHESES.equals(currentSymbol)) {
        subExpressionLevel++;
      } else if (CLOSE_PARANTHESES.equals(currentSymbol)) {
        subExpressionLevel--;
      }

      boolean isLastChar = i == expression.length() - 1;
      boolean isValidSplitIndex = operand.symbol().equals(currentSymbol) && subExpressionLevel == 0;

      if (isLastChar) {
        result.add(expression.substring(lastOperandIndex, i + 1));
      } else if (isValidSplitIndex) {
        result.add(expression.substring(lastOperandIndex, i));
        lastOperandIndex = i + 1;
      }
    }

    if (result.isEmpty()) {
      result.add(expression);
    }

    return result;
  }

}
