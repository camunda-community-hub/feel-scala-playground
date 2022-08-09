package org.example.camunda.process.solution;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.camunda.zeebe.client.ZeebeClient;
import java.io.*;
import org.example.camunda.process.solution.config.FeelTutorialConfiguration;
import org.example.camunda.process.solution.service.ZeebeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

@SpringBootTest(classes = ProcessApplication.class) // will deploy BPMN & DMN models
public class ProcessUnitTest {

  @Autowired private ZeebeClient zeebe;

  @Autowired private ZeebeService zeebeService;

  @Autowired private FeelTutorialConfiguration config;

  @Test
  public void testStartProcess() {
    String result = zeebeService.startProcess("1", "2+2");
    assertEquals("4", result);

    result = zeebeService.startProcess("2", "3+3");
    assertEquals("6", result);
  }

  @Test
  public void testUpdateDMN() throws IOException {
    File targetFile = new File("src/test/resources/result.dmn");
    Resource template = config.getDmnTemplateResource();
    assertEquals("feel-dmn-diagram.dmn", template.getFilename());
    zeebeService.updateDmn(template, "3+3", targetFile);
  }
}
