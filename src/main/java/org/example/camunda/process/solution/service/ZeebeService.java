package org.example.camunda.process.solution.service;

import static java.util.Map.entry;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.MustacheException;
import com.samskivert.mustache.Template;
import io.camunda.zeebe.client.ZeebeClient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

  public String startProcess(Map<String, Object> variables) {
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

  public InputStream generateDmn(String expression, final String decisionId) {
    try {
      final var templateData =
          Map.ofEntries(entry("expression", expression), entry("decisionId", decisionId));
      final var generatedDmn = dmnTemplate.execute(templateData);

      return new ByteArrayInputStream(generatedDmn.getBytes(StandardCharsets.UTF_8));
    } catch (MustacheException e) {
      throw new RuntimeException("Failed to generate DMN", e);
    }
  }

  public void deployDMN(InputStream is, String fileName) {
    zeebe.newDeployResourceCommand().addResourceStream(is, fileName).send().join();
  }

  public String startProcess(
      String expression, Map<String, Object> context, Map<String, String> metadata) {

    final var decisionId = config.getDecisionId() + "_" + UUID.randomUUID().toString();

    final var generatedDmn = generateDmn(expression, decisionId);
    deployDMN(generatedDmn, config.getDmnTemplateResource().getFilename());

    // TODO: How do we coordinate requests?

    //    final var variables =
    //        new ProcessVariables().setExpression(expression);

    final Map<String, Object> variables = new HashMap<>();
    variables.put("decisionId", decisionId);
    variables.putAll(context);
    variables.put("metadata", metadata);

    return startProcess(variables);
  }
}
