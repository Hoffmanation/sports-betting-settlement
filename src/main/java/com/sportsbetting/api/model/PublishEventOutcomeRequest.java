package com.sportsbetting.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PublishEventOutcomeRequest", description = "Final result of a sports event")
public class PublishEventOutcomeRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Unique sports event identifier", example = "match-42", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventId;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Display name of the event", example = "Championship Final", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventName;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Identifier of the event winner", example = "player-a", requiredMode = Schema.RequiredMode.REQUIRED)
    private String winnerId;
}
