package org.camunda.feel.playground.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlaygroundController {

  @GetMapping({"/playground", "/playground/"})
  public String getPlayground() {
    return "forward:/playground/index.html";
  }
}
