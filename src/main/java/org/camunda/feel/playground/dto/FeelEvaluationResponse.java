/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground.dto;

import java.util.List;

public final class FeelEvaluationResponse {

  public Object result;
  public String error;

  public List<FeelEvaluationWarning> warnings;

  public Object getResult() {
    return result;
  }

  public void setResult(final Object result) {
    this.result = result;
  }

  public String getError() {
    return error;
  }

  public void setError(final String error) {
    this.error = error;
  }

  public List<FeelEvaluationWarning> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<FeelEvaluationWarning> warnings) {
    this.warnings = warnings;
  }

  public static FeelEvaluationResponse withResult(Object result) {
    final var response = new FeelEvaluationResponse();
    response.setResult(result);
    return response;
  }

  public static FeelEvaluationResponse withError(String error) {
    final var response = new FeelEvaluationResponse();
    response.setError(error);
    return response;
  }

  @Override
  public String toString() {
    return "FeelEvaluationResponse{" + "result=" + result + ", error='" + error + '\'' + '}';
  }
}
