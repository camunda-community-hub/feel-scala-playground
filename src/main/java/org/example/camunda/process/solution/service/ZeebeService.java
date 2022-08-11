package org.example.camunda.process.solution.service;

import static java.util.Map.entry;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.MustacheException;
import com.samskivert.mustache.Template;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.grpc.Status;
import io.grpc.Status.Code;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.example.camunda.process.solution.FeelEvaluationResponse;
import org.example.camunda.process.solution.ProcessVariables;
import org.example.camunda.process.solution.config.FeelTutorialConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ZeebeService {

  private static final String REJECTION_PREFIX_FEEL_EXPRESSION = "Command 'CREATE' rejected with code 'INVALID_ARGUMENT': Expected to deploy new resources, but encountered the following errors:\nFEEL expression: ";

  @Autowired
  private ZeebeClient zeebe;

  @Autowired
  private FeelTutorialConfiguration config;

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

  public Object startProcess(Map<String, Object> variables) {
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
    final var resultValue = processVariables.getResult();

    if (resultValue != null) {
      return resultValue;
    } else {
      // return `null` as string to differentiate to no result
      return "null";
    }
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

  public FeelEvaluationResponse startProcess(
      String expression, Map<String, Object> context, Map<String, String> metadata) {

    final var decisionId = config.getDecisionId() + "_" + UUID.randomUUID().toString();

    final var generatedDmn = generateDmn(expression, decisionId);
    try {
      deployDMN(generatedDmn, config.getDmnTemplateResource().getFilename());
    } catch (ClientStatusException e) {
      return handleClientException(e);
    }

    final Map<String, Object> variables = new HashMap<>();
    variables.put("decisionId", decisionId);
    variables.putAll(context);
    variables.put("metadata", metadata);

    try {
      final var resultValue = startProcess(variables);
      return FeelEvaluationResponse.withResult(resultValue);
    } catch (ClientStatusException e) {
      return handleClientException(e);
    }
  }

  private FeelEvaluationResponse handleClientException(ClientStatusException e) {
    if (e.getStatusCode() == Code.DEADLINE_EXCEEDED) {
      return FeelEvaluationResponse.withError("Time out");
    } else if (e.getStatusCode() == Code.RESOURCE_EXHAUSTED) {
      return FeelEvaluationResponse.withError("Resource exhausted. Try it later again.");
    } else if (e.getStatusCode() == Code.INVALID_ARGUMENT) {
      final var shortenedMessage = e.getMessage().replace(
          REJECTION_PREFIX_FEEL_EXPRESSION, ""
      );
      return FeelEvaluationResponse.withError(shortenedMessage);
    } else {
      return FeelEvaluationResponse.withError(e.getMessage());
    }
  }
}
