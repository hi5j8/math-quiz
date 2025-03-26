package dev.jh.mathquiz.process;

import dev.jh.mathquiz.util.StringUtil;
import dev.jh.mathquiz.util.constant.Symbols;

import java.util.Arrays;

public enum Operand {

  ADDITION(Symbols.PLUS),
  SUBTRACTION(Symbols.HYPHEN),
  MULTIPLICATION(Symbols.ASTERISK),
  DIVISION(Symbols.SLASH);

  private final String symbol;

  Operand(String symbol) {
    this.symbol = symbol;
  }

  public String symbol() {
    return symbol;
  }

  public static Operand byText(String symbol) {
    if (StringUtil.hasNoContent(symbol)) {
      return null;
    }
    return Arrays.stream(Operand.values()).filter(o -> symbol.equals(o.symbol())).findFirst().orElse(null);
  }

}
