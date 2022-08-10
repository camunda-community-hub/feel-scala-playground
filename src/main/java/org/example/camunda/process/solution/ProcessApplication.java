package org.example.camunda.process.solution;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import java.util.function.Function;
import org.example.camunda.process.solution.service.ZeebeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = "classpath*:/models/*.*")
public class ProcessApplication {

  @Autowired ZeebeService zeebeService;

  public static void main(String[] args) {
    SpringApplication.run(ProcessApplication.class, args);
  }
}
