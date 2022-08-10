package org.example.camunda.process.solution;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.zeebe.client.ZeebeClient;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.example.camunda.process.solution.service.ZeebeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ProcessApplication.class) // will deploy BPMN & DMN models
public class ProcessUnitTest {

  private static final Map<String, Object> EMPTY_CONTEXT = Map.of();
  private static final Map<String, String> EMPTY_METADATA = Map.of();

  @Autowired private ZeebeClient zeebe;

  @Autowired private ZeebeService zeebeService;

  @Test
  public void shouldStartProcessWithNumericResult() {
    // given
    final var expression = "3+3";

    // when
    final var result = zeebeService.startProcess(expression, EMPTY_CONTEXT, EMPTY_METADATA);

    // then
    assertThat(result).isEqualTo("6");
  }

  @Test
  public void shouldStartProcessWithStringResult() {
    // given
    final var expression = "\"Hello world\"";

    // when
    final var result = zeebeService.startProcess(expression, EMPTY_CONTEXT, EMPTY_METADATA);

    // then
    assertThat(result).isEqualTo("Hello world");
  }

  @Test
  public void shouldStartProcessWithContext() {
    // given
    final var expression = "3 + x";

    // when
    final var result = zeebeService.startProcess(expression, Map.of("x", 5), EMPTY_METADATA);

    // then
    assertThat(result).isEqualTo("8");
  }

  @Test
  public void shouldGenerateDMN() throws IOException {
    // given
    final var expression = "3 + 3";

    // when
    final var generatedDmn = zeebeService.generateDmn(expression, "decisionId");

    // then
    final var generatedDmnAsString =
        new String(generatedDmn.readAllBytes(), StandardCharsets.UTF_8);
    assertThat(generatedDmnAsString).contains(expression);
  }
}
