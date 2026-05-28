/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground.sevice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public final class FeelMcpResourcesTest {

  @Autowired private FeelMcpResources feelMcpResources;

  @Test
  void shouldReturnFeelVersion() {
    final String response = feelMcpResources.getFeelVersion();

    assertThat(response).contains("feelEngineVersion");
    assertThat(response).doesNotContain("null");
  }
}
