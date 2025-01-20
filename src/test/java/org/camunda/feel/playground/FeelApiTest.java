/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public final class FeelApiTest {

  @Autowired private MockMvc mvc;

  @Test
  void shouldReturnResult() throws Exception {
    mvc.perform(
            post("/api/v1/feel/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"expression\": \"1 + x\", \"context\": {\"x\": 2}}"))
        .andExpect(status().isOk())
        .andExpect(content().json("{'result': 3}"));
  }

  @CsvSource(
      value = {
        "3;3",
        "\\\"feel\\\";\"feel\"",
        "true;true",
        "null;null",
        "[1,2,3];[1,2,3]",
        "{x:1};{'x': 1}",
        "[{x:1}];[{'x': 1}]",
        "{x:[1,2]};{'x': [1,2]}",
        "@\\\"2025-01-20\\\";\"2025-01-20\"",
        "@\\\"10:41:30\\\";\"10:41:30\"",
        "@\\\"10:41:30+02:00\\\";\"10:41:30+02:00\"",
        "@\\\"2025-01-20T10:41:30\\\";\"2025-01-20T10:41:30\"",
        "@\\\"2025-01-20T10:41:30+02:00\\\";\"2025-01-20T10:41:30+02:00\"",
        "@\\\"P5D\\\";\"P5D\"",
        "@\\\"P2Y\\\";\"P2Y\""
      },
      delimiter = ';')
  @ParameterizedTest
  void shouldReturnResultValue(final String expression, final String expected) throws Exception {
    mvc.perform(
            post("/api/v1/feel/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"expression\": \"" + expression + "\", \"context\": {}}"))
        .andExpect(status().isOk())
        .andExpect(content().json("{'result': " + expected + "}"));
  }

  @Test
  void shouldReturnEvaluationWarnings() throws Exception {
    mvc.perform(
            post("/api/v1/feel/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"expression\": \"x\", \"context\": {}}"))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json(
                    "{'result': null, 'warnings': [{'type': \"NO_VARIABLE_FOUND\", 'message': \"No variable found with name 'x'\"}]}"));
  }

  @Test
  void shouldReturnEvaluationFailure() throws Exception {
    mvc.perform(
            post("/api/v1/feel/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"expression\": \"assert(x, x != null)\", \"context\": {}}"))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json(
                    "{'error': \"Assertion failure on evaluate the expression 'assert(x, x != null)': The condition is not fulfilled\"}"));
  }

  @Test
  void shouldReturnParsingFailure() throws Exception {
    final var content =
        mvc.perform(
                post("/api/v1/feel/evaluate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"expression\": \"1 == 2\", \"context\": {}}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(content).contains("\"error\":\"failed to parse expression");
  }

  @Test
  void shouldEvaluateUnaryTests() throws Exception {
    mvc.perform(
            post("/api/v1/feel-unary-tests/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"expression\": \"> x\", \"inputValue\": 5, \"context\": {\"x\": 2}}"))
        .andExpect(status().isOk())
        .andExpect(content().json("{'result': true}"));
  }
}
