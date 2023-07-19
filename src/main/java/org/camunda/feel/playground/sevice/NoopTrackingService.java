package org.camunda.feel.playground.sevice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Map;

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
