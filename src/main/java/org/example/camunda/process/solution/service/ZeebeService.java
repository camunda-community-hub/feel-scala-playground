package org.example.camunda.process.solution.service;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.example.camunda.process.solution.ProcessConstants;
import org.example.camunda.process.solution.ProcessVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ZeebeService {
  @Autowired private ZeebeClient zeebe;

  public String startProcess(ProcessVariables variables) {
    ProcessInstanceEvent event =
        zeebe
            .newCreateInstanceCommand()
            .bpmnProcessId(ProcessConstants.BPMN_PROCESS_ID)
            .latestVersion()
            .variables(variables)
            .send()
            .join();

    return event.getBpmnProcessId();
  }

  public String startProcess(String... args) {
    String businessKey = args[0];
    String expression = args[1];
    final ProcessVariables variables =
        new ProcessVariables().setBusinessKey(businessKey).setExpression(expression);

    // TODO: How do we update the DMN Decision with the expression??

    return startProcess(variables);
  }
}
