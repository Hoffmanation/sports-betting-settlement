package com.sportsbetting.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BetSliceResponse", description = "Slice of bets and pagination metadata")
public class BetSliceResponse {

    @Schema(description = "Bets in the requested slice")
    private List<BetResponse> content;

    @Schema(example = "0")
    private int page;

    @Schema(example = "20")
    private int size;

    @Schema(example = "true")
    private boolean first;

    @Schema(description = "Whether another slice is available", example = "true")
    private boolean hasNext;
}
