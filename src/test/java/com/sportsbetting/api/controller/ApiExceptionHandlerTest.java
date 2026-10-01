package com.sportsbetting.api.controller;

import com.sportsbetting.domain.exception.BetNotFoundException;
import com.sportsbetting.integration.kafka.KafkaPublishException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler =
        new ApiExceptionHandler(new MockHttpServletRequest("GET", "/api/v1/bets"));

    @Test
    void returnsStructuredNotFoundResponseForMissingBet() {
        var betId = UUID.randomUUID();
        var response = handler.handleBetNotFound(new BetNotFoundException(betId));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
            .satisfies(error -> {
                assertThat(error.code()).isEqualTo("BET_NOT_FOUND");
                assertThat(error.message()).isEqualTo("Bet not found: " + betId);
                assertThat(error.error()).isEqualTo("Not Found");
                assertThat(error.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
                assertThat(error.timestamp()).isNotNull();
            });
    }

    @Test
    void returnsStructuredUnavailableResponseWhenKafkaPublishFails() {
        var exception = new KafkaPublishException("Could not publish the bet command to Kafka", new RuntimeException());
        var response = handler.handleKafkaPublishFailure(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody())
            .satisfies(error -> {
                assertThat(error.code()).isEqualTo("KAFKA_UNAVAILABLE");
                assertThat(error.message()).isEqualTo(exception.getMessage());
                assertThat(error.error()).isEqualTo("Service Unavailable");
                assertThat(error.status()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
                assertThat(error.timestamp()).isNotNull();
            });
    }
}
