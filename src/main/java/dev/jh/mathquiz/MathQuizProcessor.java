package dev.jh.mathquiz;

import dev.jh.mathquiz.config.Config;
import dev.jh.mathquiz.config.ConfigLoader;
import dev.jh.mathquiz.export.MathQuizExportException;
import dev.jh.mathquiz.export.MathQuizExporter;
import dev.jh.mathquiz.export.json.MathQuizJsonExporter;
import dev.jh.mathquiz.export.text.MathQuizTextExporter;
import dev.jh.mathquiz.process.MathQuiz;
import dev.jh.mathquiz.process.MathQuizGenerator;
import dev.jh.mathquiz.validate.config.ConfigValidator;
import dev.jh.mathquiz.validate.config.InvalidConfigException;
import dev.jh.mathquiz.validate.expression.ExpressionNodeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;


public class MathQuizProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(MathQuizProcessor.class);

  private final ConfigLoader configLoader;
  private final MathQuizGenerator mathQuizGenerator;
  private final ConfigValidator configValidator;

  public MathQuizProcessor() {
    configLoader = new ConfigLoader();
    ExpressionNodeConverter expressionNodeConverter = new ExpressionNodeConverter();
    mathQuizGenerator = new MathQuizGenerator(expressionNodeConverter);
    configValidator = new ConfigValidator();
  }

  public void start() {
    LOG.info("Starting math quiz processor");
    Instant processStart = Instant.now();

    LOG.info("Loading config to process");
    String configProfile = configLoader.getConfigProfile();
    Config config = configLoader.loadProfileConfig(configProfile);

    if (config == null) {
      LOG.info("Stopping math quiz processor (no config to process was loaded)");
      return;
    }

    LOG.info("Validating config...");
    try {
      configValidator.validate(config);
      LOG.info("Config is valid - beginning process");
    } catch (InvalidConfigException e) {
      LOG.error("Stopping math quiz processor - config is invalid ({})", e.getMessage());
      return;
    }

    LOG.info("Generating {} math quizzes...", config.quiz().amount());
    Instant generateStart = Instant.now();
    List<MathQuiz> quizzes = mathQuizGenerator.generateQuizzes(config);
    Instant generateEnd = Instant.now();
    LOG.info(
      "Finished generating {} math quizzes in {}ms",
      config.quiz().amount(), Duration.between(generateStart, generateEnd).toMillis()
    );


    MathQuizExporter exporter = switch (config.export().format()) {
      case PLAIN_TEXT -> new MathQuizTextExporter();
      case JSON -> new MathQuizJsonExporter();
    };
    LOG.info("Determined export format: {}", config.export().format());

    try {
      exporter.export(config, quizzes);
    } catch (MathQuizExportException e) {
      LOG.warn("Failed to export math quizzes ({})", e.getMessage());
    }

    Instant processEnd = Instant.now();
    LOG.info("Finished math quiz process in {}ms", Duration.between(processStart, processEnd).toMillis());
  }

}
