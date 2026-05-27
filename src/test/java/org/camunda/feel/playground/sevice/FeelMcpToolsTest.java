/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.camunda.feel.playground.dto.FeelEvaluationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public final class FeelMcpToolsTest {

  @Autowired private FeelMcpTools feelMcpTools;

  @MockitoSpyBean private TrackingService trackingService;

  @Test
  void shouldEvaluateFeelExpression() {
    final FeelEvaluationResponse response =
        feelMcpTools.evaluateFeelExpression("1 + x", Map.of("x", 2));

    assertThat(response.getResult()).isEqualTo(3L);
    assertThat(response.getError()).isNull();
  }

  @Test
  void shouldReturnErrorForInvalidFeelExpression() {
    final FeelEvaluationResponse response =
        feelMcpTools.evaluateFeelExpression("assert(x, x != null)", Map.of());

    assertThat(response.getError()).isNotNull();
    assertThat(response.getResult()).isNull();
  }

  @Test
  void shouldReturnWarningsForFeelExpression() {
    final FeelEvaluationResponse response = feelMcpTools.evaluateFeelExpression("x", Map.of());

    assertThat(response.getResult()).isNull();
    assertThat(response.getWarnings()).isNotEmpty();
    assertThat(response.getWarnings().get(0).getType()).isEqualTo("NO_VARIABLE_FOUND");
  }

  @Test
  void shouldEvaluateFeelUnaryTests() {
    final FeelEvaluationResponse response =
        feelMcpTools.evaluateFeelUnaryTests("> x", 5, Map.of("x", 2));

    assertThat(response.getResult()).isEqualTo(true);
    assertThat(response.getError()).isNull();
  }

  @Test
  void shouldTrackExpressionEvaluationWithMcpMetadata() {
    feelMcpTools.evaluateFeelExpression("1 + 1", Map.of());

    verify(trackingService).trackExpressionEvaluation(FeelMcpTools.MCP_METADATA);
  }

  @Test
  void shouldTrackUnaryTestsEvaluationWithMcpMetadata() {
    feelMcpTools.evaluateFeelUnaryTests("> 0", 5, Map.of());

    verify(trackingService).trackUnaryTestsExpressionEvaluation(FeelMcpTools.MCP_METADATA);
  }

  @Test
  void shouldTrackMetadataSourceAsMcp() {
    verify(trackingService, org.mockito.Mockito.never()).trackExpressionEvaluation(any());

    feelMcpTools.evaluateFeelExpression("1", Map.of());

    verify(trackingService)
        .trackExpressionEvaluation(
            org.mockito.ArgumentMatchers.argThat(
                metadata -> "feel-scala-mcp".equals(metadata.get("source"))));
  }
}
