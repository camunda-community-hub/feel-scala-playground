package org.camunda.feel.playground.sevice;

import java.util.Map;

public interface TrackingService {

  void trackExpressionEvaluation(Map<String, String> metadata);

  void trackUnaryTestsExpressionEvaluation(Map<String, String> metadata);
}
