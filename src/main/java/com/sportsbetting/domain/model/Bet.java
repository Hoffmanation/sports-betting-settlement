package com.sportsbetting.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record Bet(
    UUID id,
    String userId,
    String eventId,
    String eventMarketId,
    String selectedWinnerId,
    BigDecimal amount,
    BetStatus status,
    Instant createdAt,
    Instant settledAt
) {
}
