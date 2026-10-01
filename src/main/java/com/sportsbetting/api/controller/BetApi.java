package com.sportsbetting.api.controller;

import com.sportsbetting.api.model.BetResponse;
import com.sportsbetting.api.model.BetSliceResponse;
import com.sportsbetting.api.model.BetCommandAcceptedResponse;
import com.sportsbetting.api.model.CreateBetRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Bets", description = "Create bets and inspect their asynchronous settlement status")
@RequestMapping("/api/v1/bets")
public interface BetApi {

    @PostMapping
    @Operation(summary = "Place a bet", description = "Publishes a bet creation command to Kafka. The bet is persisted asynchronously by this service and becomes available to read after the command is consumed.")
    @ApiResponse(responseCode = "202", description = "Bet creation command accepted", content = @Content(schema = @Schema(implementation = BetCommandAcceptedResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid bet request", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Kafka is unavailable", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ErrorResponse.class)))
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<BetCommandAcceptedResponse> createBet(@Valid @RequestBody CreateBetRequest request);

    @GetMapping("/{betId}")
    @Operation(summary = "Get a bet", description = "Returns a bet, including its current OPEN, WON, or LOST status.")
    @ApiResponse(responseCode = "200", description = "Bet found", content = @Content(schema = @Schema(implementation = BetResponse.class)))
    @ApiResponse(responseCode = "404", description = "Bet not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ErrorResponse.class)))
    ResponseEntity<BetResponse> getBet(
        @Parameter(description = "Bet identifier", example = "fd6b6e70-8d0b-4dda-bab5-2320d2ad4d39")
        @PathVariable UUID betId
    );

    @GetMapping
    @Operation(summary = "List bets", description = "Returns a slice of bets with a hasNext indicator, optionally filtered by event identifier.")
    @ApiResponse(responseCode = "200", description = "Slice of bets", content = @Content(schema = @Schema(implementation = BetSliceResponse.class)))
    ResponseEntity<BetSliceResponse> getBets(
        @Parameter(description = "Optional event identifier filter", example = "match-42")
        @RequestParam(required = false) String eventId,
        @Parameter(description = "Zero-based page index", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @Parameter(description = "Page size (1-100)", example = "20")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    );
}
