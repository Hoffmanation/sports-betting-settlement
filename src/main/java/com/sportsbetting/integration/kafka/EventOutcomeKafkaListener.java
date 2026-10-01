package com.sportsbetting.integration.kafka;

import com.sportsbetting.application.service.EventOutcomeProcessingService;
import com.sportsbetting.domain.model.EventOutcome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOutcomeKafkaListener {

    private final JsonMapper jsonMapper;
    private final EventOutcomeProcessingService processingService;

    @KafkaListener(
        topics = "${app.kafka.topics.event-outcomes}",
        groupId = "${spring.kafka.consumer.event-outcomes-group-id}"
    )
    public void consume(String payload) {
        EventOutcome outcome;
        try {
            outcome = jsonMapper.readValue(payload, EventOutcome.class);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Invalid event outcome message", exception);
        }
        log.info("Received event outcome {} ({})", outcome.eventId(), outcome.eventName());
        processingService.process(outcome);
    }
}
