package org.camunda.feel.playground.api;

import org.camunda.feel.api.EvaluationResult;
import org.camunda.feel.playground.dto.FeelEvaluationRequest;
import org.camunda.feel.playground.dto.FeelEvaluationResponse;
import org.camunda.feel.playground.dto.FeelEvaluationWarning;
import org.camunda.feel.playground.dto.FeelUnaryTestsEvaluationRequest;
import org.camunda.feel.playground.sevice.FeelEvaluationService;
import org.camunda.feel.playground.sevice.TrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin()
public class FeelEvaluationController {

  private static final Logger LOG = LoggerFactory.getLogger(FeelEvaluationController.class);

  private final FeelEvaluationService evaluationService;
  private final TrackingService trackingService;

  public FeelEvaluationController(
      final FeelEvaluationService evaluationService, TrackingService trackingService) {
    this.evaluationService = evaluationService;
    this.trackingService = trackingService;
  }

  @PostMapping("/feel/evaluate")
  public ResponseEntity<FeelEvaluationResponse> evaluate(
      @RequestBody FeelEvaluationRequest request) {

    LOG.debug("Evaluate FEEL expression: {}", request);

    try {
      final var result = evaluationService.evaluate(request.expression, request.context);
      return createEvaluationResponse(result);

    } catch (Exception e) {
      return new ResponseEntity<>(FeelEvaluationResponse.withError(e.getMessage()), HttpStatus.OK);

    } finally {
      trackingService.trackExpressionEvaluation(request.metadata);
    }
  }

  private static ResponseEntity<FeelEvaluationResponse> createEvaluationResponse(
      EvaluationResult result) {
    final var warnings = collectEvaluationWarnings(result);

    if (result.isSuccess()) {
      final var response = FeelEvaluationResponse.withResult(result.result());
      response.setWarnings(warnings);
      return new ResponseEntity<>(response, HttpStatus.OK);

    } else {
      final var failureMessage = result.failure().message();
      final var response = FeelEvaluationResponse.withError(failureMessage);
      response.setWarnings(warnings);
      return new ResponseEntity<>(response, HttpStatus.OK);
    }
  }

  private static List<FeelEvaluationWarning> collectEvaluationWarnings(EvaluationResult result) {
    final var warnings = new ArrayList<FeelEvaluationWarning>();
    result
        .suppressedFailures()
        .foreach(
            failure -> {
              final var warning =
                  FeelEvaluationWarning.of(
                      failure.failureType().toString(), failure.failureMessage());
              warnings.add(warning);
              return null;
            });
    return warnings;
  }

  @PostMapping("/feel-unary-tests/evaluate")
  public ResponseEntity<FeelEvaluationResponse> evaluateUnaryTests(
      @RequestBody FeelUnaryTestsEvaluationRequest request) {

    LOG.debug("Evaluate FEEL unary-tests expression: {}", request);

    try {
      final var result =
          evaluationService.evaluateUnaryTests(
              request.expression, request.inputValue, request.context);

      return createEvaluationResponse(result);

    } catch (Exception e) {
      return new ResponseEntity<>(FeelEvaluationResponse.withError(e.getMessage()), HttpStatus.OK);

    } finally {
      trackingService.trackUnaryTestsExpressionEvaluation(request.metadata);
    }
  }
}
