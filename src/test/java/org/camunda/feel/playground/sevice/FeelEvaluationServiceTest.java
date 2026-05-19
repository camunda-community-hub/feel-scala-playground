/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;
import org.camunda.feel.api.EvaluationResult;
import org.camunda.feel.api.FeelEngineApi;
import org.junit.jupiter.api.Test;

public final class FeelEvaluationServiceTest {

  @Test
  void shouldReturnEvaluationResultWithinTimeout() {
    final var feelEngineApi = mock(FeelEngineApi.class);
    final var evaluationResult = mock(EvaluationResult.class);

    when(feelEngineApi.evaluateExpression(
            anyString(), org.mockito.ArgumentMatchers.<Map<String, Object>>any()))
        .thenReturn(evaluationResult);

    try (var service = new FeelEvaluationService(feelEngineApi, Duration.ofSeconds(1))) {
      final var result = service.evaluate("1 + 2", Map.of());
      assertThat(result).isEqualTo(evaluationResult);
    }
  }

  @Test
  void shouldFailWhenEvaluationTakesTooLong() {
    final var feelEngineApi = mock(FeelEngineApi.class);
    final var evaluationResult = mock(EvaluationResult.class);

    when(feelEngineApi.evaluateExpression(
            anyString(), org.mockito.ArgumentMatchers.<Map<String, Object>>any()))
        .thenAnswer(
            invocation -> {
              Thread.sleep(200);
              return evaluationResult;
            });

    try (var service = new FeelEvaluationService(feelEngineApi, Duration.ofMillis(10))) {
      assertThatThrownBy(() -> service.evaluate("1 + 2", Map.of()))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Evaluation exceeded timeout of PT0.01S");
    }
  }
}
