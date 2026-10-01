package com.sportsbetting.api.controller;

import com.sportsbetting.api.model.PublishEventOutcomeRequest;
import com.sportsbetting.api.model.PublishEventOutcomeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Event outcomes", description = "Publish final sports event outcomes to Kafka for bet settlement")
@RequestMapping("/api/v1/event-outcomes")
public interface EventOutcomeApi {

    @PostMapping
    @Operation(
        summary = "Publish an event outcome",
        description = "Publishes the event ID, name, and winner ID to the Kafka event-outcomes topic. Matching bets are queued for asynchronous settlement."
    )
    @ApiResponse(responseCode = "202", description = "Outcome acknowledged by Kafka", content = @Content(schema = @Schema(implementation = PublishEventOutcomeResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid event outcome", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Kafka is unavailable", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ErrorResponse.class)))
    ResponseEntity<PublishEventOutcomeResponse> publishOutcome(@Valid @RequestBody PublishEventOutcomeRequest request);
}
