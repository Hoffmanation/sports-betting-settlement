package com.sportsbetting.domain.model;

import com.sportsbetting.domain.event.BetCommandType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record BetCommandAccepted(
    UUID commandId,
    UUID betId,
    String eventId,
    BetCommandType commandType,
    Instant acceptedAt
) {
}
