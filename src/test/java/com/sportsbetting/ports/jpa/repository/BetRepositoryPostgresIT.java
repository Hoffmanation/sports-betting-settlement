package com.sportsbetting.ports.jpa.repository;

import com.sportsbetting.domain.model.BetStatus;
import com.sportsbetting.ports.jpa.entity.BetEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BetRepositoryPostgresIT extends AbstractPostgresRepositoryIT {

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void flywaySeedsDefaultBetsAndRepositoryReturnsSlicesWithoutTotals() {
        var firstSlice = betRepository.findAllByEventId(
            "match-42",
            PageRequest.of(0, 2, Sort.by("createdAt").ascending())
        );

        assertThat(firstSlice).hasSize(2);
        assertThat(firstSlice.hasNext()).isTrue();

        var secondSlice = betRepository.findAllByEventId(
            "match-42",
            PageRequest.of(1, 2, Sort.by("createdAt").ascending())
        );

        assertThat(secondSlice).hasSize(1);
        assertThat(secondSlice.hasNext()).isFalse();
    }

    @Test
    @Transactional
    void usesPostgresTimestampDefaultAndSettlesAnOpenBetOnlyOnce() {
        var betId = UUID.randomUUID();
        var bet = new BetEntity();
        bet.setId(betId);
        bet.setUserId("postgres-it-user");
        bet.setEventId("postgres-it-" + UUID.randomUUID());
        bet.setEventMarketId("winner-market");
        bet.setSelectedWinnerId("player-a");
        bet.setAmount(new BigDecimal("10.0000"));
        bet.setStatus(BetStatus.OPEN);

        var savedBet = betRepository.saveAndFlush(bet);
        assertThat(savedBet.getCreatedAt()).isNotNull();
        assertThat(savedBet.getVersion()).isZero();

        var settledAt = Instant.parse("2026-10-01T12:00:00Z");
        assertThat(betRepository.settleIfOpen(betId, BetStatus.OPEN, BetStatus.WON, settledAt))
            .isEqualTo(1);
        assertThat(betRepository.settleIfOpen(
            betId,
            BetStatus.OPEN,
            BetStatus.LOST,
            settledAt.plusSeconds(1)
        )).isZero();

        entityManager.clear();
        assertThat(betRepository.findById(betId))
            .get()
            .satisfies(settledBet -> {
                assertThat(settledBet.getStatus()).isEqualTo(BetStatus.WON);
                assertThat(settledBet.getSettledAt()).isEqualTo(settledAt);
                assertThat(settledBet.getVersion()).isEqualTo(1L);
            });
    }
}
