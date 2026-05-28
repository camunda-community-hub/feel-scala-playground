/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground.sevice;

import java.util.Map;

import org.camunda.feel.playground.api.VersionController;
import org.camunda.feel.playground.dto.FeelEvaluationResponse;
import org.camunda.feel.playground.dto.VersionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class FeelMcpTools {

  private static final Logger LOG = LoggerFactory.getLogger(FeelMcpTools.class);

  static final Map<String, String> MCP_METADATA = Map.of("source", "feel-scala-mcp");

  private final FeelEvaluationService evaluationService;
  private final TrackingService trackingService;

  public FeelMcpTools(
      final FeelEvaluationService evaluationService, final TrackingService trackingService) {
    this.evaluationService = evaluationService;
    this.trackingService = trackingService;
  }

  @Tool(
      name = "evaluate_feel_expression",
      description =
          "Evaluate a FEEL (Friendly Enough Expression Language) expression with an optional"
              + " context. Returns the result or an error message if the evaluation fails.")
  public FeelEvaluationResponse evaluateFeelExpression(
      @ToolParam(description = "The FEEL expression to evaluate") final String expression,
      @ToolParam(description = "The context variables as a JSON object", required = false)
          final Map<String, Object> context) {

    LOG.debug("Evaluate FEEL expression via MCP: {}", expression);

    try {
      final var result = evaluationService.evaluate(expression, context);
      return FeelEvaluationResponse.of(result);

    } catch (final Exception e) {
      return FeelEvaluationResponse.withError(e.getMessage());

    } finally {
      trackingService.trackExpressionEvaluation(MCP_METADATA);
    }
  }

  @Tool(
      name = "evaluate_feel_unary_tests",
      description =
          "Evaluate a FEEL (Friendly Enough Expression Language) unary-tests expression against an"
              + " input value with an optional context. Returns true/false or an error message.")
  public FeelEvaluationResponse evaluateFeelUnaryTests(
      @ToolParam(description = "The FEEL unary-tests expression to evaluate")
          final String expression,
      @ToolParam(description = "The input value to test against") final Object inputValue,
      @ToolParam(description = "The context variables as a JSON object", required = false)
          final Map<String, Object> context) {

    LOG.debug("Evaluate FEEL unary-tests expression via MCP: {}", expression);

    try {
      final var result = evaluationService.evaluateUnaryTests(expression, inputValue, context);
      return FeelEvaluationResponse.of(result);

    } catch (final Exception e) {
      return FeelEvaluationResponse.withError(e.getMessage());

    } finally {
      trackingService.trackUnaryTestsExpressionEvaluation(MCP_METADATA);
    }
  }

  @Tool(
      name = "get_feel_version",
      description = "Return the version of the FEEL-Scala engine used to evaluate expressions.")
  public VersionResponse getFeelVersion() {
    return VersionResponse.withVersion(VersionController.FEEL_ENGINE_VERSION);
  }
}
