package org.camunda.feel.playground.api;

import org.camunda.feel.playground.dto.FeelEvaluationRequest;
import org.camunda.feel.playground.dto.FeelEvaluationResponse;
import org.camunda.feel.playground.dto.FeelUnaryTestsEvaluationRequest;
import org.camunda.feel.playground.sevice.FeelEvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin()
public class FeelEvaluationController {

  private static final Logger LOG = LoggerFactory.getLogger(FeelEvaluationController.class);

  private final FeelEvaluationService evaluationService;

  public FeelEvaluationController(final FeelEvaluationService evaluationService) {
    this.evaluationService = evaluationService;
  }

  @PostMapping("/feel/evaluate")
  public ResponseEntity<FeelEvaluationResponse> evaluate(
      @RequestBody FeelEvaluationRequest request) {

    LOG.debug("Evaluate FEEL expression: {}", request);

    try {
      final var result = evaluationService.evaluate(request.expression, request.context);

      return new ResponseEntity<>(FeelEvaluationResponse.withResult(result), HttpStatus.OK);

    } catch (Exception e) {
      return new ResponseEntity<>(FeelEvaluationResponse.withError(e.getMessage()), HttpStatus.OK);
    }
  }

  @PostMapping("/feel-unary-tests/evaluate")
  public ResponseEntity<FeelEvaluationResponse> evaluateUnaryTests(
      @RequestBody FeelUnaryTestsEvaluationRequest request) {

    LOG.debug("Evaluate FEEL unary-tests expression: {}", request);

    try {
      final var result =
          evaluationService.evaluateUnaryTests(
              request.expression, request.inputValue, request.context);

      return new ResponseEntity<>(FeelEvaluationResponse.withResult(result), HttpStatus.OK);

    } catch (Exception e) {
      return new ResponseEntity<>(FeelEvaluationResponse.withError(e.getMessage()), HttpStatus.OK);
    }
  }
}
