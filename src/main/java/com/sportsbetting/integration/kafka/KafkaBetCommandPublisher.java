package com.sportsbetting.integration.kafka;

import com.sportsbetting.application.port.BetCommandPublisher;
import com.sportsbetting.domain.event.BetCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBetCommandPublisher implements BetCommandPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    @Value("${app.kafka.topics.bet-commands}")
    private String topic;

    @Override
    public void publish(BetCommand command) {
        String payload;
        try {
            payload = jsonMapper.writeValueAsString(command);
        } catch (JacksonException exception) {
            throw new KafkaPublishException("Could not serialize the bet command", exception);
        }

        try {
            SendResult<String, String> result = kafkaTemplate.send(topic, command.betId().toString(), payload)
                .get(10, TimeUnit.SECONDS);
            log.info(
                "Published Kafka bet command id={} type={} betId={} topic={}",
                command.commandId(),
                command.commandType(),
                command.betId(),
                result.getRecordMetadata().topic()
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KafkaPublishException("Interrupted while publishing the bet command", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new KafkaPublishException("Could not publish the bet command to Kafka", exception);
        }
    }
}
