package com.sportsbetting.api.controller;

import com.sportsbetting.api.model.BetResponse;
import com.sportsbetting.api.model.BetSliceResponse;
import com.sportsbetting.api.model.BetCommandAcceptedResponse;
import com.sportsbetting.api.model.CreateBetRequest;
import com.sportsbetting.application.service.BetApplicationService;
import com.sportsbetting.domain.mapper.BetMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class BetController implements BetApi {

    private final BetApplicationService betApplicationService;
    private final BetMapper betMapper;

    @Override
    public ResponseEntity<BetCommandAcceptedResponse> createBet(@Valid CreateBetRequest request) {
        var accepted = betApplicationService.requestBetCreation(betMapper.toDomain(request));
        log.info("Accepted bet command {} for bet {}", accepted.commandId(), accepted.betId());
        return ResponseEntity.accepted().body(betMapper.toCommandAcceptedResponse(accepted));
    }

    @Override
    public ResponseEntity<BetResponse> getBet(UUID betId) {
        log.info("Fetching bet {}", betId);
        var bet = betApplicationService.getBet(betId);
        return ResponseEntity.ok(betMapper.toResponse(bet));
    }

    @Override
    public ResponseEntity<BetSliceResponse> getBets(String eventId, int page, int size) {
        log.info("Fetching bets for event={} page={} size={}", eventId, page, size);
        var bets = betApplicationService.getBets(eventId, page, size);
        var response = BetSliceResponse.builder()
            .content(bets.content().stream().map(betMapper::toResponse).toList())
            .page(bets.page())
            .size(bets.size())
            .first(bets.first())
            .hasNext(bets.hasNext())
            .build();
        return ResponseEntity.ok(response);
    }
}
