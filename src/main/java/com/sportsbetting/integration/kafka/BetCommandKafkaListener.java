package com.sportsbetting.integration.kafka;

import com.sportsbetting.application.service.BetApplicationService;
import com.sportsbetting.domain.event.BetCommand;
import com.sportsbetting.domain.model.Bet;
import com.sportsbetting.domain.model.BetStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class BetCommandKafkaListener {

    private final JsonMapper jsonMapper;
    private final BetApplicationService betApplicationService;

    @KafkaListener(
        topics = "${app.kafka.topics.bet-commands}",
        groupId = "${spring.kafka.consumer.bet-commands-group-id}"
    )
    public void consume(String payload) {
        BetCommand command;
        try {
            command = jsonMapper.readValue(payload, BetCommand.class);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Invalid bet command message", exception);
        }

        switch (command.commandType()) {
            case BET_CREATE_REQUESTED -> createBet(command);
            default -> log.warn("Unsupported bet command type: {}", command.commandType());
        }

        log.info(
            "Consumed Kafka bet command id={} type={} betId={}",
            command.commandId(),
            command.commandType(),
            command.betId()
        );
    }

    private void createBet(BetCommand command) {
        betApplicationService.persistBetFromCommand(Bet.builder()
            .id(command.betId())
            .userId(command.userId())
            .eventId(command.eventId())
            .eventMarketId(command.eventMarketId())
            .selectedWinnerId(command.selectedWinnerId())
            .amount(command.amount())
            .status(BetStatus.OPEN)
            .createdAt(Instant.now())
            .build());
    }
}
