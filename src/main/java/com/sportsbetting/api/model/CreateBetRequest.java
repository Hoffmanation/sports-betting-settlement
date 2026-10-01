package com.sportsbetting.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreateBetRequest", description = "A user's selection and stake for a sports event")
public class CreateBetRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Identifier of the user placing the bet", example = "user-1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Identifier of the sports event", example = "match-42", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventId;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Identifier of the market within the event", example = "winner-market", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventMarketId;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Identifier of the participant selected to win", example = "player-a", requiredMode = Schema.RequiredMode.REQUIRED)
    private String selectedWinnerId;

    @NotNull
    @Positive
    @DecimalMin(value = "0.00", inclusive = false)
    @Schema(description = "Stake amount; must be greater than zero", example = "10.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;
}
