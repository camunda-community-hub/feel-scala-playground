package org.camunda.feel.playground.sevice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "playground.tracking", name = "enabled", havingValue = "true")
public class MixpanelProperties {

    @Value("${MIXPANEL_PROJECT_TOKEN}")
    private String projectToken;

    public String getProjectToken() {
        return projectToken;
    }
}
