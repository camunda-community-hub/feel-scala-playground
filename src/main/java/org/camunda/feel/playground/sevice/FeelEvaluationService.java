/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground.sevice;

import java.util.Map;
import org.camunda.feel.FeelEngine;
import org.camunda.feel.api.EvaluationResult;
import org.camunda.feel.api.FeelEngineApi;
import org.camunda.feel.impl.JavaValueMapper;
import org.springframework.stereotype.Component;

@Component
public final class FeelEvaluationService {

  private final FeelEngineApi feelEngineApi = buildFeelEngine();

  private FeelEngineApi buildFeelEngine() {
    final var feelEngine =
        new FeelEngine.Builder().customValueMapper(new JavaValueMapper()).build();
    return new FeelEngineApi(feelEngine);
  }

  public EvaluationResult evaluate(String expression, Map<String, Object> context) {
    return feelEngineApi.evaluateExpression(expression, context);
  }

  public EvaluationResult evaluateUnaryTests(
      String expression, Object inputValue, Map<String, Object> context) {
    return feelEngineApi.evaluateUnaryTests(expression, inputValue, context);
  }
}
