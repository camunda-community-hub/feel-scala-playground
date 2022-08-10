package org.example.camunda.process.solution.service;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.MustacheException;
import com.samskivert.mustache.Template;
import io.camunda.zeebe.client.ZeebeClient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.example.camunda.process.solution.ProcessVariables;
import org.example.camunda.process.solution.config.FeelTutorialConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ZeebeService {

  @Autowired private ZeebeClient zeebe;

  @Autowired private FeelTutorialConfiguration config;

  private Template dmnTemplate;

  @PostConstruct
  public void parseDmnTemplate() {
    try {
      final var inputStream = config.getDmnTemplateResource().getInputStream();
      final var reader = new InputStreamReader(inputStream);

      dmnTemplate = Mustache.compiler().compile(reader);
    } catch (Exception e) {
      throw new RuntimeException("Failed to read DMN template", e);
    }
  }

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

  public InputStream generateDmn(String expression) {
    try {
      final var templateData = Map.ofEntries(Map.entry("expression", expression));
      final var generatedDmn = dmnTemplate.execute(templateData);

      return new ByteArrayInputStream(generatedDmn.getBytes(StandardCharsets.UTF_8));
    } catch (MustacheException e) {
      throw new RuntimeException("Failed to generate DMN", e);
    }
  }

  public void deployDMN(InputStream is, String fileName) {
    zeebe.newDeployResourceCommand().addResourceStream(is, fileName).send().join();
  }

  public String startProcess(String... args) {
    String businessKey = args[0];
    String expression = args[1];

    final var generatedDmn = generateDmn(expression);
    deployDMN(generatedDmn, config.getDmnTemplateResource().getFilename());

    // TODO: How do we coordinate requests?

    final var variables =
        new ProcessVariables().setBusinessKey(businessKey).setExpression(expression);

    return startProcess(variables);
  }
}
