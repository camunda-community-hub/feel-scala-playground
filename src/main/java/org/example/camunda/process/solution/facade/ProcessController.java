package org.example.camunda.process.solution.facade;

import org.example.camunda.process.solution.FeelEvaluationRequest;
import org.example.camunda.process.solution.config.FeelTutorialConfiguration;
import org.example.camunda.process.solution.service.ZeebeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/process")
@CrossOrigin()
public class ProcessController {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);

  @Autowired private ZeebeService zeebeService;

  @Autowired private FeelTutorialConfiguration config;

  @PostMapping("/start")
  public ResponseEntity<String> startProcessInstance(@RequestBody FeelEvaluationRequest request) {

    LOG.info("Starting process `" + config.getBpmProcessId() + "` with variables: " + request);

    return new ResponseEntity<>(
        zeebeService.startProcess(
            request.getExpression(), request.getContext(), request.getMetadata()),
        HttpStatus.OK);
  }
}
