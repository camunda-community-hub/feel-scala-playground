package org.example.camunda.process.solution.facade;

import org.example.camunda.process.solution.ProcessVariables;
import org.example.camunda.process.solution.config.FeelTutorialConfiguration;
import org.example.camunda.process.solution.service.ZeebeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/process")
public class ProcessController {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);

  @Autowired private ZeebeService zeebeService;

  @Autowired private FeelTutorialConfiguration config;

  @PostMapping("/start")
  public ResponseEntity<ProcessVariables> startProcessInstance(
      @RequestBody ProcessVariables variables) {

    LOG.info("Starting process `" + config.getBpmProcessId() + "` with variables: " + variables);

    return new ResponseEntity<>(zeebeService.startProcess(variables), HttpStatus.OK);
  }
}
