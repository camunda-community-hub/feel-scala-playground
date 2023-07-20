package org.camunda.feel.playground.sevice;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(MixpanelProperties.class)
public class NoopTrackingService implements TrackingService {

  @Override
  public void trackExpressionEvaluation(Map<String, String> metadata) {
    // no-op
  }

  @Override
  public void trackUnaryTestsExpressionEvaluation(Map<String, String> metadata) {
    // no-op
  }
}
