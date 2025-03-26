package dev.jh.mathquiz.config;

public record ExportFileConfig(
  String path,
  String name,
  Boolean withTimestamp,
  Boolean overwriteExisting
) {
  public static final String PATH = "export.file.path";
  public static final String NAME = "export.file.name";
  public static final String WITH_TIMESTAMP = "export.file.with-timestamp";
  public static final String OVERWRITE_EXISTING = "export.file.overwrite-existing";
}
