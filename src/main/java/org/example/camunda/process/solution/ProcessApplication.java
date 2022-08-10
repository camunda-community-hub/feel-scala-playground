package org.example.camunda.process.solution;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import java.util.function.Function;
import org.example.camunda.process.solution.service.ZeebeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = "classpath*:/models/*.*")
public class ProcessApplication {

  @Autowired ZeebeService zeebeService;

  public static void main(String[] args) {
    SpringApplication.run(ProcessApplication.class, args);
  }

  @Bean
  public Function<Message<?>, Message<?>> enableCors() {
    return message -> {
      System.out.println("> enable CORS");
      System.out.println("> headers: " + message.getHeaders());
      return MessageBuilder.fromMessage(message)
          //          .withPayload(message.getPayload())
          //          .copyHeaders(message.getHeaders())
          .setHeader("Access-Control-Allow-Origin", "*")
          .setHeader("Access-Control-Allow-Methods", "POST")
          .build();
    };
  }

  @Bean
  public Function<FeelEvaluationRequest, String> evaluateExpression() {
    return request -> {
      System.out.println("> evaluateExpression");
      return zeebeService.startProcess(
          request.getExpression(), request.getContext(), request.getMetadata());
    };
  }

  @Bean
  public Function<Message<FeelEvaluationRequest>, Message<String>> startProcess() {
    return message -> {
      System.out.println("> startProcess");

      final var request = message.getPayload();
      final var result =
          zeebeService.startProcess(
              request.getExpression(), request.getContext(), request.getMetadata());

      System.out.println(">>> headers: " + message.getHeaders());

      return MessageBuilder.withPayload(result)
          .copyHeaders(message.getHeaders())
          .setHeader("Access-Control-Allow-Origin", "*")
          .setHeader("Access-Control-Allow-Methods", "POST")
          .build();
    };
  }
}
