/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground.sevice;

import org.camunda.feel.playground.api.VersionController;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Service;

@Service
public class FeelMcpResources {

  @McpResource(
      uri = "feel://version",
      name = "feel_version",
      description = "The version of the FEEL-Scala engine used to evaluate expressions.",
      mimeType = "application/json")
  public String getFeelVersion() {
    return "{\"feelEngineVersion\": \"" + VersionController.FEEL_ENGINE_VERSION + "\"}";
  }
}
