package org.example.camunda.process.solution.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@ConfigurationProperties(prefix = "feel.tutorial")
public class FeelTutorialConfiguration {

  private String bpmProcessId;
  private String resultVariableName;
  private String decisionId;
  private Resource dmnTemplateResource;

  public String getBpmProcessId() {
    return bpmProcessId;
  }

  public void setBpmProcessId(String bpmProcessId) {
    this.bpmProcessId = bpmProcessId;
  }

  public String getResultVariableName() {
    return resultVariableName;
  }

  public void setResultVariableName(String resultVariableName) {
    this.resultVariableName = resultVariableName;
  }

  public String getDecisionId() {
    return decisionId;
  }

  public void setDecisionId(String decisionId) {
    this.decisionId = decisionId;
  }

  public Resource getDmnTemplateResource() {
    return dmnTemplateResource;
  }

  public void setDmnTemplateResource(Resource dmnTemplateResource) {
    this.dmnTemplateResource = dmnTemplateResource;
  }
}
