package com.sportsbetting.domain.model;

import lombok.Builder;

@Builder
public record EventOutcome(
    String eventId,
    String eventName,
    String winnerId
) {
}
