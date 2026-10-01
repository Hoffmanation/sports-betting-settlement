package com.sportsbetting.api.model;

import com.sportsbetting.domain.model.BetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreateBetResponse", description = "Newly registered bet")
public class CreateBetResponse {

    @Schema(example = "fd6b6e70-8d0b-4dda-bab5-2320d2ad4d39", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID betId;

    @Schema(example = "user-1")
    private String userId;

    @Schema(example = "match-42")
    private String eventId;

    @Schema(example = "winner-market")
    private String eventMarketId;

    @Schema(example = "player-a")
    private String selectedWinnerId;

    @Schema(example = "10.00")
    private BigDecimal amount;

    @Schema(example = "OPEN", implementation = BetStatus.class)
    private BetStatus status;

    @Schema(example = "2026-09-28T09:00:00Z", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;
}
