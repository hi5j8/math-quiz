package dev.jh.mathquiz.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import dev.jh.mathquiz.export.ExportFormat;
import dev.jh.mathquiz.process.Operand;
import dev.jh.mathquiz.util.*;
import dev.jh.mathquiz.util.constant.Symbols;
import dev.jh.mathquiz.util.type.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class ConfigLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
  private static final String CONFIG_PROPERTIES = "config.properties";
  private static final String PROFILE_PROPERTIES_PATH_TEMPLATE = "profiles/%s.properties";
  private static final String PROFILES_USE = "profiles.use";

  public String getConfigProfile() {
    LOG.info("Loading config profile from '{}'", CONFIG_PROPERTIES);
    Properties properties;
    try {
      properties = FileUtil.loadProperties(CONFIG_PROPERTIES);
    } catch (IOException e) {
      LOG.error("Failed to get config profile due to exception", e);
      return null;
    }
    String configProfile = properties.getProperty(PROFILES_USE);
    if (configProfile == null) {
      LOG.warn("Could not find config profile - make sure a value for '{}' is set", PROFILES_USE);
    } else {
      LOG.info("Found config profile '{}'", configProfile);
    }
    return properties.getProperty(PROFILES_USE);
  }

  public Config loadProfileConfig(String profile) {
    if (StringUtil.hasNoContent(profile)) {
      LOG.warn("Could not load profile config (profile is null)");
      return null;
    }

    String profilePropertiesFileName = String.format(PROFILE_PROPERTIES_PATH_TEMPLATE, profile);
    LOG.info("Loading profile config from '{}'", profilePropertiesFileName);

    Properties properties;
    try {
      properties = FileUtil.loadProperties(profilePropertiesFileName);
    } catch (IOException e) {
      LOG.error("Failed to load profile config profile due to exception:", e);
      return null;
    }

    ExportConfig exportConfig = loadExportConfig(properties);
    QuizConfig quizConfig = loadQuizConfig(properties);
    LinkedList<VariableConfig> variableConfigs = loadVariableConfigs(properties);
    LinkedList<EquationConfig> equationConfigs = loadEquationConfigs(properties);

    Config result = new Config(exportConfig, quizConfig, variableConfigs, equationConfigs);
    debugLogLoadedConfig(result);
    return result;
  }

  private ExportConfig loadExportConfig(Properties properties) {
    ExportFormat exportFormat = ExportFormat.byText(properties.getProperty(ExportConfig.FORMAT));
    String exportPath = properties.getProperty(ExportFileConfig.PATH);
    String exportFileName = properties.getProperty(ExportFileConfig.NAME);

    Boolean exportFileWithTimestamp = PropertiesUtil
      .getBoolean(properties, ExportFileConfig.WITH_TIMESTAMP, null);

    Boolean exportFileOverwriteExisting = PropertiesUtil
      .getBoolean(properties, ExportFileConfig.OVERWRITE_EXISTING, null);

    return new ExportConfig(
      exportFormat,
      new ExportFileConfig(exportPath, exportFileName, exportFileWithTimestamp, exportFileOverwriteExisting)
    );
  }

  private QuizConfig loadQuizConfig(Properties properties) {
    Integer amount = PropertiesUtil.getInteger(properties, QuizConfig.AMOUNT, null);
    LinkedList<Integer> equationOrder = PropertiesUtil.getIntegerLinkedList(
      properties, QuizConfig.EQUATION_ORDER, Symbols.COMMA, null
    );
    return new QuizConfig(amount, equationOrder);
  }

  private LinkedList<VariableConfig> loadVariableConfigs(Properties properties) {
    LinkedList<VariableConfig> result = new LinkedList<>();

    Map<Integer, Properties> propertiesPerIndex =
      PropertiesUtil.toPropertiesPerIndexMap(properties, VariableConfig.PREFIX);

    NavigableMap<Integer, Properties> orderedPropertiesPerIndex =
      CollectionUtil.toKeySortedMap(propertiesPerIndex, true);

    orderedPropertiesPerIndex.forEach(
      (index, variableProperties) -> result.add(loadVariableConfig(variableProperties, index))
    );
    return result;
  }

  private VariableConfig loadVariableConfig(Properties properties, int index) {
    String identifier = properties.getProperty(String.format(VariableConfig.IDENTIFIER, index));
    Range range = PropertiesUtil.getRange(properties, String.format(VariableConfig.RANGE, index), Symbols.COMMA, null);
    return new VariableConfig(identifier, range);
  }

  private LinkedList<EquationConfig> loadEquationConfigs(Properties properties) {
    LinkedList<EquationConfig> result = new LinkedList<>();

    Map<Integer, Properties> propertiesPerIndex =
      PropertiesUtil.toPropertiesPerIndexMap(properties, EquationConfig.PREFIX);

    NavigableMap<Integer, Properties> orderedPropertiesPerIndex =
      CollectionUtil.toKeySortedMap(propertiesPerIndex, true);

    orderedPropertiesPerIndex.forEach(
      (index, variableProperties) -> result.add(loadEquationConfig(variableProperties, index))
    );
    return result;
  }

  private EquationConfig loadEquationConfig(Properties properties, int index) {
    List<String> operandStrings = PropertiesUtil
      .getStringList(properties, String.format(EquationConfig.OPERANDS, index), Symbols.COMMA, null);
    List<Operand> operands = new ArrayList<>();
    for (String operandString : operandStrings) {
      Operand operand = Operand.byText(operandString);
      if (operand == null) {
        LOG.warn(
          "Could not resolve operand for value '{}' defined in '{}'",
          operandString, String.format(EquationConfig.OPERANDS, index)
        );
      } else {
        operands.add(operand);
      }
    }

    Integer variablesAmount = PropertiesUtil.getInteger(
      properties, String.format(EquationVariableConfig.AMOUNT, index), null
    );
    List<List<String>> variableCombos = PropertiesUtil.getStringListList(
      properties, String.format(EquationVariableConfig.COMBOS, index), Symbols.POUND, Symbols.COMMA, null
    );
    Boolean variablesAsIdentifiers = PropertiesUtil.getBoolean(
      properties, String.format(EquationVariableConfig.AS_IDENTIFIERS, index), null
    );
    Boolean mutationEnabled = PropertiesUtil.getBoolean(
      properties, String.format(EquationMutationConfig.ENABLED, index), null
    );
    BigDecimal mutationChance = PropertiesUtil.getBigDecimal(
      properties, String.format(EquationMutationConfig.CHANCE, index), null
    );
    BigDecimal mutationMultiplier = PropertiesUtil.getBigDecimal(
      properties, String.format(EquationMutationConfig.MULTIPLIER, index), null
    );
    Range mutationAmplifier = PropertiesUtil.getRange(
      properties, String.format(EquationMutationConfig.AMPLIFIER, index), Symbols.COMMA, null
    );
    List<String> mutationAffectedVariables = PropertiesUtil.getStringList(
      properties, String.format(EquationMutationConfig.AFFECTED_VARIABLES, index), Symbols.COMMA, null
    );
    Boolean validateSelfDivision = PropertiesUtil.getBoolean(
      properties, String.format(EquationValidationConfig.SELF_DIVISION, index), null
    );
    Boolean validateSelfSubtraction = PropertiesUtil.getBoolean(
      properties, String.format(EquationValidationConfig.SELF_SUBTRACTION, index), null
    );
    Boolean validateNegativeResults = PropertiesUtil.getBoolean(
      properties, String.format(EquationValidationConfig.NEGATIVE_RESULTS, index), null
    );
    Boolean validateDecimalResults = PropertiesUtil.getBoolean(
      properties, String.format(EquationValidationConfig.DECIMAL_RESULTS, index), null
    );
    Boolean validateDecimalSolution = PropertiesUtil.getBoolean(
      properties, String.format(EquationValidationConfig.DECIMAL_SOLUTION, index), null
    );
    Range solutionRange = PropertiesUtil.getRange(
      properties, String.format(EquationSolutionConfig.RANGE, index), Symbols.COMMA, null
    );
    Boolean hideSolution = PropertiesUtil.getBoolean(
      properties, String.format(EquationSolutionConfig.HIDE, index), null
    );
    return new EquationConfig(
      operands,
      new EquationVariableConfig(
        variablesAmount, variableCombos, variablesAsIdentifiers
      ),
      new EquationMutationConfig(
        mutationEnabled, mutationChance, mutationMultiplier, mutationAmplifier, mutationAffectedVariables
      ),
      new EquationValidationConfig(
        validateSelfDivision, validateSelfSubtraction, validateNegativeResults,
        validateDecimalResults, validateDecimalSolution
      ),
      new EquationSolutionConfig(
        solutionRange, hideSolution
      )
    );
  }

  private void debugLogLoadedConfig(Config config) {
    try {
      ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
      String json = objectWriter.writeValueAsString(config);
      LOG.debug("Loaded config with the following values:\n{}", json);
    } catch (JsonProcessingException e) {
      LOG.debug("Failed to log loaded config as JSON:", e);
    }

  }

}
