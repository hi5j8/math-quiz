package dev.jh.mathquiz.validate.expression;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import dev.jh.mathquiz.process.Operand;
import dev.jh.mathquiz.util.ExpressionUtil;
import dev.jh.mathquiz.util.NumberUtil;
import dev.jh.mathquiz.util.StringUtil;
import dev.jh.mathquiz.util.type.Range;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ExpressionValidator {

  private final ExpressionNodeConverter expressionNodeConverter;

  private boolean checkSelfDivision;
  private boolean checkSelfSubtraction;
  private boolean checkNegativeResults;
  private boolean checkDecimalResults;
  private boolean checkDecimalSolution;
  private Range solutionRange;

  public ExpressionValidator(ExpressionNodeConverter expressionNodeConverter) {
    this.expressionNodeConverter = expressionNodeConverter;
  }

  public ExpressionValidator prohibitSelfDivision(boolean value) {
    this.checkSelfDivision = value;
    return this;
  }

  public ExpressionValidator prohibitSelfSubtraction(boolean value) {
    this.checkSelfSubtraction = value;
    return this;
  }

  public ExpressionValidator prohibitNegativeResults(boolean value) {
    this.checkNegativeResults = value;
    return this;
  }

  public ExpressionValidator prohibitDecimalResults(boolean value) {
    this.checkDecimalResults = value;
    return this;
  }

  public ExpressionValidator prohibitDecimalSolution(boolean value) {
    this.checkDecimalSolution = value;
    return this;
  }

  public ExpressionValidator keepWithinSolutionRange(Range range) {
    solutionRange = range;
    return this;
  }

  public BigDecimal solveAndValidate(String expression) throws InvalidExpressionException {
    if (expression == null || expression.isBlank()) {
      throw new InvalidExpressionException("Expression is null or empty");
    }

    ExpressionNode expressionTree = expressionNodeConverter.toExpressionNode(expression);
    if (expressionTree == null) {
      throw new InvalidExpressionException("Expression tree is null");
    }

    if (expressionTree.nodes() == null || expressionTree.nodes().isEmpty()) {
      if (expressionTree.value() == null || expressionTree.value().isEmpty()) {
        throw new InvalidExpressionException("Expression tree is a single, invalid node without value");
      } else if (!ExpressionUtil.isIsolatedValue(expressionTree.value())) {
        throw new InvalidExpressionException(String.format(
          "Expression node '%s' cannot be a leaf note and contain an expression as a value at the same time",
          expressionTree.value()
        ));
      } else {
        // root node is isolated value with no child nodes
        return new BigDecimal(expressionTree.value());
      }
    }

    // check if overall solution is within range
    BigDecimal result = solveNode(expressionTree);
    if (solutionRange != null && !NumberUtil.isWithinRange(solutionRange, result)) {
      throw new InvalidExpressionException(String.format(
        "Result of expression '%s' (%f) is outside the defined solution range (%s-%s)",
        expression, result.floatValue(), solutionRange.min().toPlainString(), solutionRange.max().toPlainString())
      );
    }

    // check if overall solution is decimal result
    if (checkDecimalSolution && isDecimalResult(result)) {
      throw new InvalidExpressionException(String.format(
        "Expression '%s' equates to decimal result: '%s", expression, result.toPlainString()
      ));
    }

    return result;
  }

  private BigDecimal solveNode(ExpressionNode node) throws InvalidExpressionException {
    BigDecimal result = null;
    int index = 0;

    if (node.hasNodes() && checkSelfDivision && isSelfSubtraction(node)) {
      throw new InvalidExpressionException(String.format("Found self-subtraction at node '%s'", node.value()));
    }

    // calculate each child node together successively
    while (index < node.nodes().size()) {
      ExpressionNode nextNode;

      try {
        nextNode = node.nodes().get(index + 1);
      } catch (IndexOutOfBoundsException e) {
        // if no next node available -> loop finished
        break;
      }

      Operand operand = node.operand();
      String rightSide = nextNode.hasNodes() ? solveNode(nextNode).toPlainString() : nextNode.value();
      String leftSide;

      if (result == null) {
        // first calculation: calculate current and next node
        ExpressionNode currentNode = node.nodes().get(index);
        leftSide = currentNode.hasNodes() ? solveNode(currentNode).toPlainString() : currentNode.value();
      } else {
        // subsequent calculation: calculate result and next node
        leftSide = result.toPlainString();
      }

      String expressionString = leftSide + operand.symbol() + rightSide;

      if (checkSelfDivision && isSelfDivision(leftSide, operand, rightSide)) {
        throw new InvalidExpressionException(String.format(
          "Found self-division at node '%s': '%s'", node.value(), expressionString
        ));
      }

      try {
        Expression expression = new Expression(expressionString);
        result = expression.evaluate().getNumberValue();
        index++;
      } catch (EvaluationException | ParseException e) {
        throw new InvalidExpressionException("Failed to solve expression node", e);
      }

      String equationString = expressionString + "=" + result;

      if (checkNegativeResults && isNegativeResult(result)) {
        throw new InvalidExpressionException(String.format(
          "Found negative result at node '%s': '%s'", node.value(), equationString
        ));
      }

      if (checkDecimalResults && isDecimalResult(result)) {
        throw new InvalidExpressionException(String.format(
          "Found decimal result at node '%s': '%s", node.value(), equationString
        ));
      }
    }

    return result;
  }

  private boolean isSelfDivision(String left, Operand operand, String right) {
     return operand.equals(Operand.DIVISION) && left.equals(right);
  }

  private boolean isSelfSubtraction(ExpressionNode node) {
    if (!Operand.SUBTRACTION.equals(node.operand()) || !node.hasNodes()) {
      return false;
    }
    for (int i = 0; i < node.nodes().size(); i++) {
      ExpressionNode currentChildNode = node.nodes().get(i);
      ExpressionNode nextChildNode;
      try {
        nextChildNode = node.nodes().get(i + 1);
      } catch (IndexOutOfBoundsException e) {
        // last node reached
        break;
      }
      // both current and next child node have the same value
      boolean isSameNodeValue = currentChildNode.value().equals(nextChildNode.value());

      // check if current node is specifically addition and if the last child node of current node is the same value
      // as the next node value (e.g. for case "(a+b)-b-a-a-b", where the b in "(a+b)" is relevant)
      boolean currentNodeIsAdditionWithSameLastValue = !ExpressionUtil.isSubExpression(currentChildNode.value())
        && currentChildNode.hasNodes()
        && Operand.ADDITION.equals(currentChildNode.operand())
        && currentChildNode.nodes().getLast().value().equals(nextChildNode.value());

      if (isSameNodeValue || currentNodeIsAdditionWithSameLastValue) {
        return true;
      }
    }
    return false;
  }

  private boolean isNegativeResult(BigDecimal result) {
    return result.compareTo(new BigDecimal(0)) < 0;
  }

  private boolean isDecimalResult(BigDecimal result) {
    return result.stripTrailingZeros().scale() > 0;
  }

  private Operand findLastOperand(String expression) {
    Operand result = null;
    if (StringUtil.hasNoContent(expression)) {
      return result;
    }
    for (int i = 0; i < expression.length(); i++) {
      String s = String.valueOf(expression.charAt(i));
      Operand operand = Operand.byText(s);
      if (operand != null) {
        result = operand;
      }
    }
    return result;
  }

}
