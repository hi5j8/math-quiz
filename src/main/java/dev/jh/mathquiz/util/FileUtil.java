package dev.jh.mathquiz.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class FileUtil {

  private FileUtil() {
  }

  public static Path createFile(String fileName, boolean createDirectories) throws IOException {
    return createFile(Paths.get(fileName), createDirectories);
  }

  public static Path createFile(Path filePath, boolean createDirectories) throws IOException {
    if (createDirectories) {
      Path parentDir = filePath.getParent();
      System.out.println(parentDir.getFileName());
      Files.createDirectories(parentDir);
    }
    return Files.createFile(filePath);
  }

  public static Properties loadProperties(String filePath) throws IOException {
    InputStream inputStream = Files.newInputStream(Path.of(filePath));
    Properties properties = new Properties();
    properties.load(inputStream);
    return properties;
  }

}
