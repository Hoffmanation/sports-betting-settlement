package com.sportsbetting.ports.jpa.repository;

import com.sportsbetting.domain.model.BetStatus;
import com.sportsbetting.ports.jpa.entity.BetEntity;
import jakarta.persistence.EntityManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(BetRepositoryTest.CacheTestConfiguration.class)
class BetRepositoryTest {

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private EntityManager entityManager;

    @TestConfiguration(proxyBeanMethods = false)
    static class CacheTestConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("betsById", "betsSearch");
        }
    }

    @Test
    void usesDatabaseDefaultsAndSettlesEachBetOnlyOnce() {
        UUID betId = UUID.randomUUID();
        String eventId = "test-" + UUID.randomUUID();
        var bet = new BetEntity();
        bet.setId(betId);
        bet.setUserId("user-1");
        bet.setEventId(eventId);
        bet.setEventMarketId("winner-market");
        bet.setSelectedWinnerId("player-a");
        bet.setAmount(new BigDecimal("10.00"));
        bet.setStatus(BetStatus.OPEN);
        var savedBet = betRepository.saveAndFlush(bet);

        assertThat(savedBet.getCreatedAt()).isNotNull();
        assertThat(savedBet.getVersion()).isZero();
        var openBets = betRepository.findAllByEventIdAndStatus(
            eventId,
            BetStatus.OPEN,
            org.springframework.data.domain.PageRequest.of(0, 10)
        );
        assertThat(openBets).hasSize(1);
        assertThat(openBets.hasNext()).isFalse();

        assertThat(betRepository.settleIfOpen(
            betId,
            BetStatus.OPEN,
            BetStatus.WON,
            Instant.parse("2025-01-02T00:00:00Z")
        )).isEqualTo(1);
        assertThat(betRepository.settleIfOpen(
            betId,
            BetStatus.OPEN,
            BetStatus.LOST,
            Instant.parse("2025-01-03T00:00:00Z")
        )).isZero();

        entityManager.clear();
        assertThat(betRepository.findById(betId))
            .get()
            .satisfies(settledBet -> {
                assertThat(settledBet.getStatus()).isEqualTo(BetStatus.WON);
                assertThat(settledBet.getVersion()).isEqualTo(1L);
                assertThat(settledBet.getSettledAt()).isEqualTo(Instant.parse("2025-01-02T00:00:00Z"));
            });
    }
}
