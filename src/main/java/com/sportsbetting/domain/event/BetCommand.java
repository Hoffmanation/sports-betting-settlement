package com.sportsbetting.domain.event;

import com.sportsbetting.domain.model.Bet;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record BetCommand(
    UUID commandId,
    BetCommandType commandType,
    UUID betId,
    Instant occurredAt,
    String userId,
    String eventId,
    String eventMarketId,
    String selectedWinnerId,
    BigDecimal amount
) {

    public static BetCommand create(Bet bet) {
        return BetCommand.builder()
            .commandId(UUID.randomUUID())
            .commandType(BetCommandType.BET_CREATE_REQUESTED)
            .betId(bet.id())
            .occurredAt(Instant.now())
            .userId(bet.userId())
            .eventId(bet.eventId())
            .eventMarketId(bet.eventMarketId())
            .selectedWinnerId(bet.selectedWinnerId())
            .amount(bet.amount())
            .build();
    }
}
