/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.camunda.feel.FeelEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public final class VersionApiTest {

  @Autowired private MockMvc mvc;

  @Test
  void shouldReturnVersion() throws Exception {
    var expectedVersion = FeelEngine.class.getPackage().getImplementationVersion();
    assertThat(expectedVersion)
            .describedAs("The version should match the pattern `x.y.z`")
            .matches("(\\d+).(\\d+).(\\d+)");

    mvc.perform(get("/api/v1/version"))
        .andExpect(status().isOk())
        .andExpect(content().json("{'feelEngineVersion': '" + expectedVersion + "'}"));
  }
}
