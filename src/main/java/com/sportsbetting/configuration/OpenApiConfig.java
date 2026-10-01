package com.sportsbetting.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
    title = "Sports Betting Settlement API",
    version = "v1",
    description = "Place bets, publish sports event outcomes to Kafka, and inspect asynchronous bet settlements."
))
public class OpenApiConfig {
}
