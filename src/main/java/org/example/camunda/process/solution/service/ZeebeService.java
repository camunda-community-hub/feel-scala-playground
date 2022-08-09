package org.example.camunda.process.solution.service;

import io.camunda.zeebe.client.ZeebeClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.camunda.bpm.model.dmn.instance.Text;
import org.example.camunda.process.solution.ProcessVariables;
import org.example.camunda.process.solution.config.FeelTutorialConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ZeebeService {
  @Autowired private ZeebeClient zeebe;

  @Autowired private FeelTutorialConfiguration config;

  public String startProcess(ProcessVariables variables) {
    final var processInstanceResult =
        zeebe
            .newCreateInstanceCommand()
            .bpmnProcessId(config.getBpmProcessId())
            .latestVersion()
            .variables(variables)
            .withResult()
            .fetchVariables(config.getResultVariableName())
            .send()
            .join();

    final var processVariables = processInstanceResult.getVariablesAsType(ProcessVariables.class);
    return processVariables.getResult();
  }

  public void updateDmn(Resource template, String expression, File outputFile) throws IOException {

    DmnModelInstance dmnModelInstance = Dmn.readModelFromStream(template.getInputStream());

    Decision decision = dmnModelInstance.getModelElementById(config.getDecisionId());

    LiteralExpression existingExpression = (LiteralExpression) decision.getExpression();
    Collection<Text> texts = existingExpression.getChildElementsByType(Text.class);
    Text text = texts.iterator().next();
    text.setTextContent(expression);

    Dmn.writeModelToFile(outputFile, dmnModelInstance);
  }

  public void deployDMN(InputStream is, String fileName) {
    zeebe.newDeployResourceCommand().addResourceStream(is, fileName).send().join();
  }

  public String startProcess(String... args) {
    String businessKey = args[0];
    String expression = args[1];

    // TODO: Error handling ?
    try {
      File targetFile = File.createTempFile("feel", "dmn");

      Resource template = config.getDmnTemplateResource();
      updateDmn(template, expression, targetFile);

      deployDMN(new FileInputStream(targetFile), template.getFilename());

      // TODO: How do we coordinate requests?

      final var variables =
          new ProcessVariables().setBusinessKey(businessKey).setExpression(expression);

      return startProcess(variables);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
