package com.sportsbetting.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BetCommandAcceptedResponse", description = "Acknowledgement that a bet creation command was accepted")
public class BetCommandAcceptedResponse {

    @Schema(example = "fd6b6e70-8d0b-4dda-bab5-2320d2ad4d39", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID commandId;

    @Schema(example = "a6fd7c84-b949-4d9d-bf5c-2a57e92cf490", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID betId;

    @Schema(example = "BET_CREATE_REQUESTED")
    private String commandType;

    @Schema(example = "2026-10-01T09:00:00Z", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant acceptedAt;
}
