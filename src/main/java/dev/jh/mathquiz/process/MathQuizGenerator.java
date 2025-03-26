package dev.jh.mathquiz.process;

import dev.jh.mathquiz.config.Config;
import dev.jh.mathquiz.config.EquationConfig;
import dev.jh.mathquiz.config.VariableConfig;
import dev.jh.mathquiz.util.NumberUtil;
import dev.jh.mathquiz.util.constant.Symbols;
import dev.jh.mathquiz.util.type.Pair;
import dev.jh.mathquiz.util.type.Range;
import dev.jh.mathquiz.validate.expression.ExpressionNodeConverter;
import dev.jh.mathquiz.validate.expression.ExpressionValidator;
import dev.jh.mathquiz.validate.expression.InvalidExpressionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MathQuizGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(MathQuizGenerator.class);

  private static final int QUIZ_ATTEMPTS_BEFORE_EXCEPTION = 100;
  private static final int EQUATION_ATTEMPTS_BEFORE_EXCEPTION = 100;
  private static final int VARIABLE_ATTEMPTS_BEFORE_EXCEPTION = 100;

  private final ExpressionNodeConverter expressionNodeConverter;

  public MathQuizGenerator(ExpressionNodeConverter expressionNodeConverter) {
    this.expressionNodeConverter = expressionNodeConverter;
  }

  public List<MathQuiz> generateQuizzes(Config config) {
    List<MathQuiz> result = new ArrayList<>();
    for (int i = 1; i <= config.quiz().amount(); i++) {
      int attempt = 1;
      MathQuiz quiz = null;
      while (attempt < QUIZ_ATTEMPTS_BEFORE_EXCEPTION) {
        quiz = generateQuiz(config);
        if (quiz != null) {
          break;
        }
        attempt++;
      }
      if (quiz != null) {
        result.add(quiz);
      } else {
        LOG.warn(
          "Failed to generate quiz #{} of {} after {} attempts",
          i, config.quiz().amount(), QUIZ_ATTEMPTS_BEFORE_EXCEPTION
        );
      }
    }
    return result;
  }

  public MathQuiz generateQuiz(Config config) {
    List<Variable> variables = generateVariables(config.variables());
    if (variables.isEmpty()) {
      return null;
    }
    LinkedList<Pair<String, String>> equations = generateEquations(variables, config.equations());
    return equations.isEmpty() ? null : new MathQuiz(variables, equations);
  }

  private List<Variable> generateVariables(List<VariableConfig> configs) {
    List<Variable> result = new ArrayList<>();
    for (int i = 1; i <= configs.size(); i++) {
      VariableConfig config = configs.get(i - 1);
      int attempt = 1;
      Variable generatedVariable = null;
      while (attempt < VARIABLE_ATTEMPTS_BEFORE_EXCEPTION) {
        BigDecimal value = new BigDecimal(NumberUtil.randomInt(
          config.range().min().intValue(), config.range().max().intValue()
        ));
        boolean valueTaken = false;
        for (Variable variable : result) {
          if (variable.value().compareTo(value) == 0) {
            valueTaken = true;
            break;
          }
        }
        if (!valueTaken) {
          generatedVariable = new Variable(config.identifier(), value);
          break;
        } else {
          attempt++;
        }
      }
      if (generatedVariable != null) {
        result.add(generatedVariable);
      } else {
        LOG.warn("Failed to generate variable after {} attempts", VARIABLE_ATTEMPTS_BEFORE_EXCEPTION);
      }
    }
    return result;
  }

  private LinkedList<Pair<String, String>> generateEquations(
    List<Variable> variables, LinkedList<EquationConfig> configs
  ) {
    LinkedList<Pair<String, String>> result = new LinkedList<>();
    for (int i = 1; i <= configs.size(); i++) {
      int attempt = 1;
      Pair<String, String> equations = null;
      while (attempt < EQUATION_ATTEMPTS_BEFORE_EXCEPTION) {
        try {
          equations = generateEquation(variables, configs.get(i - 1));
          break;
        } catch (InvalidExpressionException | IllegalStateException e) {
          attempt++;
        }
      }
      if (equations != null) {
        result.add(equations);
      } else {
        LOG.warn("Failed to generate equation after {} attempts", EQUATION_ATTEMPTS_BEFORE_EXCEPTION);
      }
    }
    return result;
  }

  private Pair<String, String> generateEquation(
    List<Variable> variables, EquationConfig config
  ) throws InvalidExpressionException, IllegalStateException {
    // pick operands to use in expression
    LinkedList<Operand> orderedOperands = new LinkedList<>();
    for (int i = 0; i < config.variables().amount() - 1; i++) {
      int randomIndex = NumberUtil.randomInt(0, config.operands().size() - 1);
      orderedOperands.add(config.operands().get(randomIndex));
    }

    // pick one of the available variable combos to use
    List<String> variableCombo = config.variables().combos().get(
      NumberUtil.randomInt(0, config.variables().combos().size() - 1)
    );

    // filter out all allowed variables to use
    List<Variable> allowedVariables = variables.stream()
      .filter(variable -> variableCombo.contains(variable.identifier())).toList();

    // pick variables to use in expression
    LinkedList<Variable> orderedVariables = new LinkedList<>();
    // first, add all variables required by variable combo
    for (String identifier : variableCombo) {
      if (orderedVariables.size() == config.variables().amount()) {
        break;
      }
      allowedVariables.stream()
        .filter(v -> identifier.equals(v.identifier()))
        .findFirst().ifPresent(orderedVariables::add);
    }

    // if there's still space: fill remaining slots randomly
    while (orderedVariables.size() < config.variables().amount()) {
      int randomIndex = NumberUtil.randomInt(0, allowedVariables.size() - 1);
      orderedVariables.add(allowedVariables.get(randomIndex));
    }

    // validate that list sizes are correct
    if (orderedOperands.size() != orderedVariables.size() - 1) {
      throw new IllegalStateException(String.format(
        "Illegal state: expected amount of operands: %d, actual: %d",
        orderedVariables.size() - 1, orderedOperands.size()
      ));
    }

    // build expression string with variable values hidden
    String expressionWithIdentifiers = buildExpression(orderedVariables, orderedOperands, config);

    // build separate expression string, where identifiers are replaced with their values
    String expressionWithValues = replaceIdentifiersWithValues(expressionWithIdentifiers, orderedVariables);

    // set up validation
    ExpressionValidator expressionValidator = new ExpressionValidator(expressionNodeConverter)
      .keepWithinSolutionRange(config.solution().range())
      .prohibitSelfDivision(Boolean.TRUE.equals(config.validate().selfDivision()))
      .prohibitSelfSubtraction(Boolean.TRUE.equals(config.validate().selfSubtraction()))
      .prohibitNegativeResults(Boolean.TRUE.equals(config.validate().negativeResults()))
      .prohibitDecimalResults(Boolean.TRUE.equals(config.validate().decimalResults()))
      .prohibitDecimalSolution(Boolean.TRUE.equals(config.validate().decimalSolution()));

    // solve and validate expression
    BigDecimal expressionResult = expressionValidator.solveAndValidate(expressionWithValues);

    String solutionPart = config.solution().hide() ? Symbols.QUESTION_MARK : expressionResult.toPlainString();
    String finalEquationWithIdentifiers = expressionWithIdentifiers + Symbols.EQUALS + solutionPart;
    String finalEquationWithValues = expressionWithValues + Symbols.EQUALS + solutionPart;

    return new Pair<>(finalEquationWithIdentifiers, finalEquationWithValues);
  }

  private String buildExpression(List<Variable> variables, List<Operand> operands, EquationConfig config) {
    StringBuilder resultBuilder = new StringBuilder();

    for (int i = 0; i < variables.size(); i++) {
      Variable variable = variables.get(i);
      String variableExpression;
      if (config.mutation().enabled() && config.mutation().affectedVariables().contains(variable.identifier())) {
        variableExpression = mutateVariable(
          variable, config.mutation().chance(), config.mutation().multiplier(), config.mutation().amplifier()
        );
      } else {
        variableExpression = variable.identifier();
      }
      Operand operand = i == operands.size() ? null : operands.get(i);
      resultBuilder.append(variableExpression);
      if (operand != null) {
        resultBuilder.append(operand.symbol());
      }
    }

    return resultBuilder.toString();
  }

  private String mutateVariable(
    Variable variable, BigDecimal chance, BigDecimal multiplier, Range amplifier
  ) {
    boolean mutate = NumberUtil.randomInt(0, 100) < (chance.floatValue() * 100);
    if (!mutate) {
      return variable.identifier();
    }

    String multipliedExpressionString;
    String amplifiedExpressionString;

    // apply multiplier
    if (multiplier.equals(new BigDecimal("0.25")) || multiplier.equals(new BigDecimal("0.5"))) {
      multipliedExpressionString = Symbols.PARENTHESES_OPEN + variable.identifier()
        + Operand.MULTIPLICATION.symbol() + multiplier.toPlainString() + Symbols.PARENTHESIS_CLOSE;
    } else {
      multipliedExpressionString = variable.identifier();
    }

    // apply amplifier
    int amplifierValue = NumberUtil.randomInt(amplifier.min().intValue(), amplifier.max().intValue());
    if (amplifierValue != 1) {
      amplifiedExpressionString = "(" + amplifierValue + Operand.MULTIPLICATION.symbol()
        + multipliedExpressionString + ")";
    } else {
      amplifiedExpressionString = multipliedExpressionString;
    }

    return amplifiedExpressionString;
  }

  private String replaceIdentifiersWithValues(String expressionWithIdentifiers, List<Variable> variables) {
    String result = expressionWithIdentifiers;
    for (Variable variable : variables) {
      result = result.replace(variable.identifier(), variable.value().toPlainString());
    }
    return result;
  }

}
