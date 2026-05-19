package org.camunda.feel.playground.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/playground")
@CrossOrigin()
public class PlaygroundController {

  @GetMapping
  public String getPlayground() {
    return "redirect:/playground/index.html";
  }
}
