package com.sportsbetting.application.service;

import com.sportsbetting.domain.model.BetSettlementMessage;
import com.sportsbetting.domain.model.BetStatus;
import com.sportsbetting.ports.jpa.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetSettlementService {

    private final BetRepository betRepository;

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "betsById", key = "#message.betId()"),
        @CacheEvict(cacheNames = "betsSearch", allEntries = true)
    })
    public void settle(BetSettlementMessage message) {
        BetStatus result = Objects.equals(message.selectedWinnerId(), message.winnerId())
            ? BetStatus.WON
            : BetStatus.LOST;
        int updated = betRepository.settleIfOpen(
            message.betId(),
            BetStatus.OPEN,
            result,
            Instant.now()
        );
        if (updated == 1) {
            log.info("Settled bet {} as {}", message.betId(), result);
        } else {
            log.info("Skipping settlement for bet {}: already settled or not found", message.betId());
        }
    }
}
