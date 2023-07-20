package org.camunda.feel.playground.sevice;

import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import java.util.Map;
import java.util.Optional;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(MixpanelProperties.class)
public class MixpanelTrackingService implements TrackingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MixpanelTrackingService.class);

  private final MixpanelAPI mixpanelAPI = new MixpanelAPI();

  private final MessageBuilder messageBuilder;

  public MixpanelTrackingService(MixpanelProperties mixpanelProperties) {
    messageBuilder = new MessageBuilder(mixpanelProperties.getProjectToken());
  }

  private void sendMixpanelEvent(EVENT_TYPE eventType, Map<String, String> metadata) {

    var properties = new JSONObject();
    Optional.ofNullable(metadata).ifPresent(entry -> entry.forEach(properties::put));

    var message = messageBuilder.event(null, eventType.name(), properties);

    try {
      mixpanelAPI.sendMessage(message);
    } catch (Exception e) {
      LOGGER.warn("Failed to send Mixpanel event.", e);
    }
  }

  @Override
  public void trackExpressionEvaluation(Map<String, String> metadata) {
    sendMixpanelEvent(EVENT_TYPE.FEEL_EXPRESSION_EVALUATION, metadata);
  }

  @Override
  public void trackUnaryTestsExpressionEvaluation(Map<String, String> metadata) {
    sendMixpanelEvent(EVENT_TYPE.FEEL_UNARY_TESTS_EXPRESSION_EVALUATION, metadata);
  }

  private enum EVENT_TYPE {
    FEEL_EXPRESSION_EVALUATION,
    FEEL_UNARY_TESTS_EXPRESSION_EVALUATION
  }
}
