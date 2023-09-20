package org.camunda.feel.playground.dto;

public final class FeelEvaluationWarning {

  public String type;
  public String message;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public static FeelEvaluationWarning of(String type, String message) {
      final var warning = new FeelEvaluationWarning();
      warning.type = type;
      warning.message = message;
      return warning;
  }

  @Override
  public String toString() {
    return "FeelEvaluationWarning{" + "type='" + type + '\'' + ", message='" + message + '\'' + '}';
  }
}
