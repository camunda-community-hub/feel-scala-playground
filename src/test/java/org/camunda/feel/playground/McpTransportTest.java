/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package org.camunda.feel.playground;

import static org.assertj.core.api.Assertions.assertThat;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Verifies that the MCP server is reachable via the Streamable HTTP transport. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public final class McpTransportTest {

  @LocalServerPort private int port;

  private McpSyncClient client;

  @BeforeEach
  void setUp() {
    client =
        McpClient.sync(
                HttpClientStreamableHttpTransport.builder("http://localhost:" + port)
                    .endpoint("/mcp")
                    .build())
            .build();
    client.initialize();
  }

  @AfterEach
  void tearDown() {
    client.close();
  }

  @Test
  void shouldListTools() {
    final var tools = client.listTools().tools();

    assertThat(tools)
        .extracting(McpSchema.Tool::name)
        .contains("evaluate_feel_expression", "evaluate_feel_unary_tests");
  }

  @Test
  void shouldCallTool() {
    final var result =
        client.callTool(
            new McpSchema.CallToolRequest(
                "evaluate_feel_expression", Map.of("expression", "1 + 2")));

    assertThat(result.isError()).isNotEqualTo(true);
    assertThat(result.content())
        .filteredOn(TextContent.class::isInstance)
        .map(c -> ((TextContent) c).text())
        .anyMatch(text -> text.contains("3"));
  }
}
