package dev.jh.mathquiz.process;

import java.math.BigDecimal;

public record Variable(
  String identifier,
  BigDecimal value
) {
}
