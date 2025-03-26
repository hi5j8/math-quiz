package dev.jh.mathquiz.validate.config;

import dev.jh.mathquiz.config.Config;
import dev.jh.mathquiz.config.EquationConfig;
import dev.jh.mathquiz.config.VariableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConfigValidator {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigValidator.class);

  public void validate(Config config) throws InvalidConfigException {
    // check if config and individual configs are null
    if (config == null) {
      throw new InvalidConfigException("config is null");
    } else if (config.export() == null) {
      throw new InvalidConfigException("export config is null");
    } else if (config.quiz() == null) {
      throw new InvalidConfigException("quiz config is null");
    } else if (config.variables() == null || config.variables().isEmpty()) {
      throw new InvalidConfigException("config does not contain any variables");
    } else if (config.equations() == null || config.equations().isEmpty()) {
      throw new InvalidConfigException("config does not contain any equations");
    }

    // check if export format is set
    if (config.export().format() == null) {
      throw new InvalidConfigException("missing or invalid export format");
    }

    // check if quiz amount is valid
    if (config.quiz().amount() < 1) {
      throw new InvalidConfigException(String.format(
        "invalid amount of quizzes to export set: %d", config.quiz().amount()
      ));
    }

    // warn if amount is unusually big
    if (config.quiz().amount() > 10_000) {
      LOG.warn("generating more than 10_000 quizzes ({}) may take longer than expected", config.quiz().amount());
    }

    // check if equation order is set
    if (config.quiz().equationOrder() == null || config.quiz().equationOrder().isEmpty()) {
      throw new InvalidConfigException("equation order must be set");
    }

    // check individual variables
    for (int i = 1; i <= config.variables().size(); i++) {
      VariableConfig variable = config.variables().get(i - 1);
      // check if identifier is set
      if (variable.identifier() == null || variable.identifier().isBlank()) {
        throw new InvalidConfigException(String.format("variable #%d has no identifier set", i));
      }

      // check if range is set
      if (variable.range() == null) {
        throw new InvalidConfigException(String.format(
          "variable #%d (%s) has no range set", i, variable.identifier()
        ));
      }

      // check if range is valid
      if (variable.range().min().compareTo(variable.range().max()) > 0) {
        throw new InvalidConfigException(String.format(
          "range minimum (%s) of variable #%d is bigger than its maximum (%s)",
          variable.range().min().toPlainString(), i, variable.range().max().toPlainString()
        ));
      }
    }

    // check individual equations
    for (int i = 1; i < config.equations().size(); i++) {
      EquationConfig equation = config.equations().get(i - 1);
      // warn if no operands are set
      if (equation.operands() == null || equation.operands().isEmpty()) {
        LOG.warn("no operands set for equation #{} - will use all operands", i);
      }

      // check if amount of variables is valid
      if (equation.variables().amount() < 2) {
        throw new InvalidConfigException(String.format(
          "invalid amount of variable for equation #%s - must be at least 2", equation.variables().amount()
        ));
      }

      // warn if amount of variables is unusually high
      if (equation.variables().amount() > 10) {
        LOG.warn(
          "generating equations with more than 10 variables may take significantly longer (equation #{}: {} variables)",
          i, equation.variables().amount()
        );
      }

      // check if variable combos are set
      if (equation.variables().combos() == null || equation.variables().combos().isEmpty()) {
        throw new InvalidConfigException(String.format(
          "equation #%d is missing variable combos (must have at least one combo with at least one variable set)", i
        ));
      }

      // check if solution range is set and if min and max are correct
      if (equation.solution().range() == null) {
        throw new InvalidConfigException(String.format("equation #%d has no solution range set", i));
      }

      // check if solution range is valid
      if (equation.solution().range().min().compareTo(equation.solution().range().max()) > 0) {
        throw new InvalidConfigException(String.format(
          "solution range minimum (%s) of equation #%d is bigger than its maximum (%s)",
          equation.solution().range().min().toPlainString(), i, equation.solution().range().max().toPlainString()
        ));
      }

      // if mutation is enabled: check if all required values are set
      if (equation.mutation().enabled()) {
        // check if mutation chance is set
        if (equation.mutation().chance() == null) {
          throw new InvalidConfigException(String.format("equation #%d is missing mutation chance", i));
        }

        // check if mutation chance is within bounds
        if (equation.mutation().chance().floatValue() < 0 || equation.mutation().chance().floatValue() > 1) {
          throw new InvalidConfigException(String.format(
            "invalid mutation chance value (%f) for equation #%d",
            equation.mutation().chance().floatValue(), i
          ));
        }

        // check if mutation multiplier is set
        if (equation.mutation().multiplier() == null) {
          throw new InvalidConfigException(String.format("equation #%d is missing mutation multiplier", i));
        }

        // check if mutation multiplier is valid
        float mutationMultiplier = equation.mutation().multiplier().floatValue();
        if (mutationMultiplier != 0.25f && mutationMultiplier != 0.5f && mutationMultiplier != 1f) {
          throw new InvalidConfigException(String.format("invalid mutation multiplier set for equation #%d", i));
        }

        // check if amplifier is set
        if (equation.mutation().amplifier() == null) {
          throw new InvalidConfigException(String.format("equation #%d is missing mutation amplifier", i));
        }

        // check if amplifier is valid
        if (equation.mutation().amplifier().min().intValue() < 1) {
          throw new InvalidConfigException(String.format(
            "mutation amplifier minimum for equation #%d must be at least 1",i
          ));
        } else if (equation.mutation().amplifier().min().compareTo(equation.mutation().amplifier().max()) > 0) {
          throw new InvalidConfigException(String.format(
            "mutation amplifier range minimum (%s) of equation #%d is bigger than its maximum (%s)",
            equation.mutation().amplifier().min().toPlainString(), i,
            equation.mutation().amplifier().max().toPlainString()
          ));
        }

        // warn is no mutation-affected variables are defined
        if (equation.mutation().affectedVariables() == null || equation.mutation().affectedVariables().isEmpty()) {
          LOG.warn("no variables to be affected by mutation defined for equation #{}", i);
        }

        // check if all defined mutation-affected variables match individual variables
        if (equation.mutation().affectedVariables() != null) {
          for (String identifier : equation.mutation().affectedVariables()) {
            boolean exists = false;
            for (VariableConfig variable : config.variables()) {
              if (variable.identifier() != null && identifier.equals(variable.identifier())) {
                exists = true;
                break;
              }
            }
            if (!exists) {
              throw new InvalidConfigException(String.format(
                "mutation-affected variable '%s' defined for equation #%d does not match any defined variable",
                identifier, i
              ));
            }
          }
        }
      }
    }
  }

}
