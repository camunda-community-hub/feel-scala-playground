/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground.sevice;

import java.util.HashMap;
import java.util.Map;
import org.camunda.feel.FeelEngine;
import org.camunda.feel.impl.JavaValueMapper;
import org.springframework.stereotype.Component;

@Component
public final class FeelEvaluationService {

  private final FeelEngine feelEngine =
      new FeelEngine.Builder().customValueMapper(new JavaValueMapper()).build();

  public Object evaluate(String expression, Map<String, Object> context) {
    final var evaluationResult = feelEngine.evalExpression(expression, context);

    if (evaluationResult.isRight()) {
      return evaluationResult.right().get();

    } else {
      final var failure = evaluationResult.left().get();
      throw new RuntimeException(failure.message());
    }
  }

  public Object evaluateUnaryTests(String expression, Object inputValue, Map<String, Object> context) {
    final var contextWithInput = new HashMap<>(context);
    contextWithInput.put("cellInput", inputValue); // FeelEngine.UnaryTests.defaultInputVariable()

    final var evaluationResult = feelEngine.evalUnaryTests(expression, contextWithInput);

    if (evaluationResult.isRight()) {
      return evaluationResult.right().get();

    } else {
      final var failure = evaluationResult.left().get();
      throw new RuntimeException(failure.message());
    }
  }

}
