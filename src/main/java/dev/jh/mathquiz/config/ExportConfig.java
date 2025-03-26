package dev.jh.mathquiz.config;

import dev.jh.mathquiz.export.ExportFormat;

public record ExportConfig(
  ExportFormat format,
  ExportFileConfig file
) {
  public static final String FORMAT = "export.format";
}
