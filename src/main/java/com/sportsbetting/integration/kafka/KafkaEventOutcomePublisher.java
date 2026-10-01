package com.sportsbetting.integration.kafka;

import com.sportsbetting.application.port.EventOutcomePublisher;
import com.sportsbetting.domain.model.EventOutcome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventOutcomePublisher implements EventOutcomePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    @Value("${app.kafka.topics.event-outcomes}")
    private String topic;

    @Override
    public void publish(EventOutcome outcome) {
        String payload;
        try {
            payload = jsonMapper.writeValueAsString(outcome);
        } catch (JacksonException exception) {
            throw new KafkaPublishException("Could not serialize the event outcome", exception);
        }

        try {
            SendResult<String, String> result = kafkaTemplate.send(topic, outcome.eventId(), payload)
                .get(10, TimeUnit.SECONDS);
            log.info("Published event outcome {} to Kafka topic {}", outcome.eventId(), result.getRecordMetadata().topic());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KafkaPublishException("Interrupted while publishing the event outcome", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new KafkaPublishException("Could not publish the event outcome to Kafka", exception);
        }
    }
}
