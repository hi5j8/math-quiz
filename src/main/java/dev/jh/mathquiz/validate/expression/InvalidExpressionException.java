package dev.jh.mathquiz.validate.expression;

public class InvalidExpressionException extends Exception {

  public InvalidExpressionException(String message) {
    super(message);
  }

  public InvalidExpressionException(String message, Throwable cause) {
    super(message, cause);
  }

}
