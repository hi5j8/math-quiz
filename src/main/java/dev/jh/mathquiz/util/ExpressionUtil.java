package dev.jh.mathquiz.util;

import dev.jh.mathquiz.process.Operand;
import dev.jh.mathquiz.util.constant.Symbols;

public final class ExpressionUtil {

  private ExpressionUtil() {
  }

  public static boolean isIsolatedValue(String expression) {
    for (Operand operand : Operand.values()) {
      if (expression.contains(operand.symbol())) {
        return false;
      }
    }
    return true;
  }

  public static boolean isSubExpression(String expression) {
    return StringUtil.hasContent(expression)
      && expression.startsWith(Symbols.PARENTHESES_OPEN)
      && expression.endsWith(Symbols.PARENTHESIS_CLOSE);
  }

}
