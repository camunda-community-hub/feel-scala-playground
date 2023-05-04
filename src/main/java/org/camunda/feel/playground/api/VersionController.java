package org.camunda.feel.playground.api;

import org.camunda.feel.FeelEngine;
import org.camunda.feel.playground.dto.VersionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/version")
@CrossOrigin()
public class VersionController {

  private static final String feelEngineVersion = FeelEngine.class.getPackage()
      .getImplementationVersion();

  @GetMapping
  public ResponseEntity<VersionResponse> getVersion() {
    return new ResponseEntity<>(VersionResponse.withVersion(feelEngineVersion), HttpStatus.OK);
  }
}
