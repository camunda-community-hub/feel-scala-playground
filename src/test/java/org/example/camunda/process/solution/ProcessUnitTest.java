package org.example.camunda.process.solution;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.zeebe.client.ZeebeClient;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.example.camunda.process.solution.service.ZeebeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ProcessApplication.class) // will deploy BPMN & DMN models
public class ProcessUnitTest {

  @Autowired private ZeebeClient zeebe;

  @Autowired private ZeebeService zeebeService;

  @Test
  public void shouldStartProcessWithNumericResult() {
    // given
    final var expression = "3+3";

    // when
    final var result = zeebeService.startProcess("2", expression);

    // then
    assertThat(result).isEqualTo("6");
  }

  @Test
  public void shouldStartProcessWithStringResult() {
    // given
    final var expression = "\"Hello world\"";

    // when
    final var result = zeebeService.startProcess("2", expression);

    // then
    assertThat(result).isEqualTo("Hello world");
  }

  @Test
  public void shouldGenerateDMN() throws IOException {
    // given
    final var expression = "3 + 3";

    // when
    final var generatedDmn = zeebeService.generateDmn(expression);

    // then
    final var generatedDmnAsString =
        new String(generatedDmn.readAllBytes(), StandardCharsets.UTF_8);
    assertThat(generatedDmnAsString).contains(expression);
  }
}
