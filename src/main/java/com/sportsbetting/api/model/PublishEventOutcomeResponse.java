package com.sportsbetting.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PublishEventOutcomeResponse", description = "Acknowledgement that the event outcome was accepted by Kafka")
public class PublishEventOutcomeResponse {

    @Schema(example = "match-42")
    private String eventId;

    @Schema(example = "PUBLISHED")
    private PublishEventOutcomeStatus status;

    @Schema(example = "2026-09-28T09:00:00Z")
    private Instant publishedAt;
}
