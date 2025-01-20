/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground.sevice;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.camunda.feel.FeelEngine;
import org.camunda.feel.api.EvaluationResult;
import org.camunda.feel.api.FeelEngineApi;
import org.camunda.feel.impl.JavaValueMapper;
import org.camunda.feel.syntaxtree.*;
import org.camunda.feel.valuemapper.CustomValueMapper;
import org.camunda.feel.valuemapper.JavaCustomValueMapper;
import org.springframework.stereotype.Component;

@Component
public final class FeelEvaluationService {

  private final FeelEngineApi feelEngineApi = buildFeelEngine();

  private FeelEngineApi buildFeelEngine() {
    final var feelEngine =
        new FeelEngine.Builder()
            .customValueMapper(new JavaValueMapperWithTemporalStringDeserialization())
            .build();
    return new FeelEngineApi(feelEngine);
  }

  public EvaluationResult evaluate(String expression, Map<String, Object> context) {
    return feelEngineApi.evaluateExpression(expression, context);
  }

  public EvaluationResult evaluateUnaryTests(
      String expression, Object inputValue, Map<String, Object> context) {
    return feelEngineApi.evaluateUnaryTests(expression, inputValue, context);
  }

  private static class JavaValueMapperWithTemporalStringDeserialization
      extends JavaCustomValueMapper {

    private final CustomValueMapper baseValueMapper = new JavaValueMapper();

    @Override
    public Optional<Val> toValue(Object x, Function<Object, Val> innerValueMapper) {
      return baseValueMapper.toVal(x, innerValueMapper::apply).fold(Optional::empty, Optional::of);
    }

    @Override
    public Optional<Object> unpackValue(Val value, Function<Val, Object> innerValueMapper) {
      if (isTemporalValue(value)) {
        return Optional.of(value.toString());
      } else {
        return baseValueMapper
            .unpackVal(value, innerValueMapper::apply)
            .fold(Optional::empty, Optional::of);
      }
    }

    private static boolean isTemporalValue(Val value) {
      return value instanceof ValDate
          || value instanceof ValTime
          || value instanceof ValLocalTime
          || value instanceof ValDateTime
          || value instanceof ValLocalDateTime
          || value instanceof ValDayTimeDuration
          || value instanceof ValYearMonthDuration;
    }
  }
}
