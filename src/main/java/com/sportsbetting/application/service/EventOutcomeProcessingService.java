package com.sportsbetting.application.service;

import com.sportsbetting.application.port.BetSettlementPublisher;
import com.sportsbetting.domain.mapper.BetMapper;
import com.sportsbetting.domain.model.BetSettlementMessage;
import com.sportsbetting.domain.model.BetStatus;
import com.sportsbetting.domain.model.EventOutcome;
import com.sportsbetting.ports.jpa.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventOutcomeProcessingService {

    private final BetRepository betRepository;
    private final BetMapper betMapper;
    private final BetSettlementPublisher betSettlementPublisher;

    @Value("${app.settlement.match-page-size:500}")
    private int matchPageSize = 500;

    @Transactional(readOnly = true)
    public void process(EventOutcome outcome) {
        var sort = Sort.by("createdAt").ascending().and(Sort.by("id").ascending());
        var pageable = PageRequest.of(0, matchPageSize, sort);
        var bets = betRepository.findAllByEventIdAndStatus(outcome.eventId(), BetStatus.OPEN, pageable);

        while (!bets.isEmpty()) {
            bets.forEach(entity -> publishSettlement(outcome, entity));
            var lastBet = bets.getContent().getLast();
            if (!bets.hasNext()) {
                break;
            }
            bets = betRepository.findNextOpenBets(
                outcome.eventId(),
                BetStatus.OPEN,
                lastBet.getCreatedAt(),
                lastBet.getId(),
                pageable
            );
        }
    }

    private void publishSettlement(EventOutcome outcome, com.sportsbetting.ports.jpa.entity.BetEntity entity) {
        var bet = betMapper.toDomain(entity);
        betSettlementPublisher.publish(BetSettlementMessage.builder()
            .betId(bet.id())
            .eventId(outcome.eventId())
            .selectedWinnerId(bet.selectedWinnerId())
            .winnerId(outcome.winnerId())
            .build());
        log.info("Queued settlement for bet {} from event {}", bet.id(), outcome.eventId());
    }
}
