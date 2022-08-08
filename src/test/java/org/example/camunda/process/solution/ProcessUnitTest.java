package org.example.camunda.process.solution;

import static org.junit.Assert.assertNotNull;

import io.camunda.zeebe.client.ZeebeClient;
import org.example.camunda.process.solution.service.ZeebeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ProcessApplication.class) // will deploy BPMN & DMN models
public class ProcessUnitTest {

  @Autowired private ZeebeClient zeebe;

  @Autowired private ZeebeService zeebeService;

  @Test
  public void testStartProcess() {
    String instanceId = zeebeService.startProcess("23", "2+2");
    assertNotNull(instanceId);
  }
}
