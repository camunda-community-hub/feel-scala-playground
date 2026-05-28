/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground.sevice;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import org.camunda.feel.FeelEngine;
import org.camunda.feel.api.EvaluationResult;
import org.camunda.feel.api.FeelEngineApi;
import org.camunda.feel.impl.JavaValueMapper;
import org.camunda.feel.syntaxtree.*;
import org.camunda.feel.valuemapper.CustomValueMapper;
import org.camunda.feel.valuemapper.JavaCustomValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class FeelEvaluationService implements AutoCloseable {

  private final FeelEngineApi feelEngineApi;
  private final Duration evaluationTimeout;
  private final ExecutorService executorService;

  @Autowired
  public FeelEvaluationService(FeelEvaluationProperties feelEvaluationProperties) {
    this(buildFeelEngine(), feelEvaluationProperties.getTimeout());
  }

  FeelEvaluationService(FeelEngineApi feelEngineApi, Duration evaluationTimeout) {
    this.feelEngineApi = feelEngineApi;
    this.evaluationTimeout = evaluationTimeout;
    this.executorService = Executors.newCachedThreadPool();
  }

  private static FeelEngineApi buildFeelEngine() {
    final var feelEngine =
        new FeelEngine.Builder()
            .customValueMapper(new JavaValueMapperWithTemporalStringDeserialization())
            .build();
    return new FeelEngineApi(feelEngine);
  }

  public EvaluationResult evaluate(String expression, Map<String, Object> context) {
      final var evaluationContext = Optional.ofNullable(context).orElse(Collections.emptyMap());
      return evaluateWithTimeout(() -> feelEngineApi.evaluateExpression(expression, evaluationContext));
  }

  public EvaluationResult evaluateUnaryTests(
      String expression, Object inputValue, Map<String, Object> context) {
    final var evaluationContext = Optional.ofNullable(context).orElse(Collections.emptyMap());
    return evaluateWithTimeout(
        () -> feelEngineApi.evaluateUnaryTests(expression, inputValue, evaluationContext));
  }

  private EvaluationResult evaluateWithTimeout(Supplier<EvaluationResult> evaluation) {
    final Future<EvaluationResult> future = executorService.submit(evaluation::get);

    try {
      return future.get(evaluationTimeout.toMillis(), TimeUnit.MILLISECONDS);

    } catch (TimeoutException e) {
      future.cancel(true);
      throw new IllegalStateException("Evaluation exceeded timeout of " + evaluationTimeout, e);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Evaluation interrupted", e);

    } catch (ExecutionException e) {
      if (e.getCause() instanceof RuntimeException runtimeException) {
        throw runtimeException;
      }
      throw new IllegalStateException(e.getCause());
    }
  }

  @PreDestroy
  @Override
  public void close() {
    executorService.shutdownNow();
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
