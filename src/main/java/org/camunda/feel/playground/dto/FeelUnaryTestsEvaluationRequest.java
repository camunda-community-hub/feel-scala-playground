package org.camunda.feel.playground.dto;

import java.util.Map;

public class FeelUnaryTestsEvaluationRequest {

  public String expression;
  public Object inputValue;
  public Map<String, Object> context;
  public Map<String, String> metadata;

  public String getExpression() {
    return expression;
  }

  public void setExpression(final String expression) {
    this.expression = expression;
  }

  public Map<String, Object> getContext() {
    return context;
  }

  public void setContext(final Map<String, Object> context) {
    this.context = context;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(final Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public Object getInputValue() {
    return inputValue;
  }

  public void setInputValue(final Object inputValue) {
    this.inputValue = inputValue;
  }

  @Override
  public String toString() {
    return "FeelUnaryTestsEvaluationRequest{"
        + "expression='"
        + expression
        + '\''
        + ", inputValue="
        + inputValue
        + ", context="
        + context
        + ", metadata="
        + metadata
        + '}';
  }
}
