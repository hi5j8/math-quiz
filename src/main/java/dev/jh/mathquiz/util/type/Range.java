package dev.jh.mathquiz.util.type;

import java.math.BigDecimal;

public record Range(
  BigDecimal min,
  BigDecimal max
) {
}
