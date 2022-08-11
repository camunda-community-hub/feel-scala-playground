package org.example.camunda.process.solution;

import java.util.Map;

public class FeelEvaluationRequest {

  public String expression;
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

  @Override
  public String toString() {
    return "FeelEvaluationRequest{" +
        "expression='" + expression + '\'' +
        ", context=" + context +
        ", metadata=" + metadata +
        '}';
  }
}
