package com.sportsbetting.api.controller;

import com.sportsbetting.api.model.PublishEventOutcomeRequest;
import com.sportsbetting.api.model.PublishEventOutcomeResponse;
import com.sportsbetting.api.model.PublishEventOutcomeStatus;
import com.sportsbetting.application.port.EventOutcomePublisher;
import com.sportsbetting.application.service.EventOutcomeRegistrationService;
import com.sportsbetting.domain.mapper.BetMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EventOutcomeController implements EventOutcomeApi {

    private final EventOutcomeRegistrationService eventOutcomeRegistrationService;
    private final EventOutcomePublisher eventOutcomePublisher;
    private final BetMapper betMapper;

    @Override
    public ResponseEntity<PublishEventOutcomeResponse> publishOutcome(@Valid PublishEventOutcomeRequest request) {
        var outcome = betMapper.toDomain(request);
        boolean newlyRegistered = eventOutcomeRegistrationService.persistIfAbsent(outcome);
        if (!newlyRegistered) {
            var response = PublishEventOutcomeResponse.builder()
                .eventId(outcome.eventId())
                .status(PublishEventOutcomeStatus.ALREADY_PUBLISHED)
                .publishedAt(Instant.now())
                .build();
            return ResponseEntity.ok(response);
        }

        eventOutcomePublisher.publish(outcome);
        log.info("Accepted event outcome for event {}", outcome.eventId());
        var response = PublishEventOutcomeResponse.builder()
            .eventId(outcome.eventId())
            .status(PublishEventOutcomeStatus.PUBLISHED)
            .publishedAt(Instant.now())
            .build();
        return ResponseEntity.accepted().body(response);
    }
}
