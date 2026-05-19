package org.camunda.feel.playground;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public final class PlaygroundUiTest {

  @Autowired private MockMvc mvc;

  @Test
  void shouldRedirectPlaygroundEndpoint() throws Exception {
    mvc.perform(get("/playground"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/playground/index.html"));
  }

  @Test
  void shouldServePlaygroundHtml() throws Exception {
    final var content =
        mvc.perform(get("/playground/index.html"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(content).contains("FEEL-Scala Playground");
    assertThat(content).contains("Copy Share Link");
    assertThat(content).contains("Copy Result");
    assertThat(content).contains("Import Share Link");
    assertThat(content).contains("Format JSON");
    assertThat(content).contains("expression-type");
    assertThat(content).contains("evaluation-time");
    assertThat(content).contains("import-panel");
    assertThat(content).contains("expression-highlight");
    assertThat(content).contains("context-highlight");
    assertThat(content).contains("/playground/app.js");
    assertThat(content).contains("/playground/styles.css");
    assertThat(content).contains("/webjars/bootstrap/5.3.8/css/bootstrap.min.css");
    assertThat(content).doesNotContain("cdn.jsdelivr.net");
  }

  @Test
  void shouldServePlaygroundAssets() throws Exception {
    mvc.perform(get("/playground/app.js")).andExpect(status().isOk());
    mvc.perform(get("/playground/styles.css")).andExpect(status().isOk());
    mvc.perform(get("/webjars/bootstrap/5.3.8/css/bootstrap.min.css")).andExpect(status().isOk());
  }
}
