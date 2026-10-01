package com.sportsbetting.api.controller;

import com.sportsbetting.api.model.PublishEventOutcomeRequest;
import com.sportsbetting.api.model.PublishEventOutcomeResponse;
import com.sportsbetting.application.port.EventOutcomePublisher;
import com.sportsbetting.domain.mapper.BetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EventOutcomeController implements EventOutcomeApi {

    private final EventOutcomePublisher eventOutcomePublisher;
    private final BetMapper betMapper;

    @Override
    public ResponseEntity<PublishEventOutcomeResponse> publishOutcome(PublishEventOutcomeRequest request) {
        var outcome = betMapper.toDomain(request);
        eventOutcomePublisher.publish(outcome);
        log.info("Accepted event outcome for event {}", outcome.eventId());
        var response = PublishEventOutcomeResponse.builder()
            .eventId(outcome.eventId())
            .status("PUBLISHED")
            .publishedAt(Instant.now())
            .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
