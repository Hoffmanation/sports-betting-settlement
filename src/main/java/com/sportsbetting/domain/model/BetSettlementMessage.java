package com.sportsbetting.domain.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record BetSettlementMessage(
    UUID betId,
    String eventId,
    String selectedWinnerId,
    String winnerId
) {
}
