package com.sportsbetting.configuration;

import com.sportsbetting.domain.model.Bet;
import com.sportsbetting.domain.model.BetSlice;
import com.sportsbetting.domain.model.BetStatus;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CacheConfigTest {

    @Test
    void configuresBetCachesAndSerializesDomainSlices() {
        CacheManager cacheManager = new CacheConfig().cacheManager(
            mock(RedisConnectionFactory.class),
            300
        );

        cacheManager.getCache("betsById");
        assertThat(cacheManager.getCache("betsSearch")).isNotNull();
        assertThat(cacheManager.getCacheNames()).containsExactlyInAnyOrder("betsById", "betsSearch");

        var bet = Bet.builder()
            .id(UUID.randomUUID())
            .userId("user-1")
            .eventId("match-42")
            .eventMarketId("winner-market")
            .selectedWinnerId("player-a")
            .amount(new BigDecimal("10.00"))
            .status(BetStatus.OPEN)
            .createdAt(Instant.parse("2026-10-01T09:00:00Z"))
            .build();
        var betSlice = BetSlice.builder()
            .content(List.of(bet))
            .page(0)
            .size(20)
            .first(true)
            .hasNext(false)
            .build();
        RedisCache cache = (RedisCache) cacheManager.getCache("betsSearch");
        ByteBuffer serialized = cache.getCacheConfiguration()
            .getValueSerializationPair()
            .write(betSlice);

        assertThat(cache.getCacheConfiguration()
            .getValueSerializationPair()
            .read(serialized))
            .isEqualTo(betSlice);
    }
}
