package com.sportsbetting.integration.rocketmq;

import com.sportsbetting.application.port.BetSettlementPublisher;
import com.sportsbetting.domain.model.BetSettlementMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockRocketMqProducer implements BetSettlementPublisher {

    private final MockRocketMqBroker broker;

    @Value("${app.rocketmq.topic:bet-settlements}")
    private String topic;

    @Override
    public void publish(BetSettlementMessage message) {
        broker.send(message);
        log.info("Published settlement to RocketMQ mock topic {}: {}", topic, message);
    }
}
