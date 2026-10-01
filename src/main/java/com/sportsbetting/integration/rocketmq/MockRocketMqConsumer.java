package com.sportsbetting.integration.rocketmq;

import com.sportsbetting.application.service.BetSettlementService;
import com.sportsbetting.domain.model.BetSettlementMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockRocketMqConsumer {

    private final MockRocketMqBroker broker;
    private final BetSettlementService settlementService;

    @Value("${app.rocketmq.topic:bet-settlements}")
    private String topic;

    // TODO - Implement a more sophisticated scheduling mechanism to handle message consumption and retries
    //  in case of failures (Dead Letter Queue)
    @Scheduled(fixedDelayString = "${app.rocketmq.mock.poll-interval:200}")
    public void consumeNext() {
        BetSettlementMessage message = broker.peek();
        if (message == null) {
            return;
        }
        settlementService.settle(message);
        if (broker.acknowledge(message)) {
            log.info("Consumed settlement from RocketMQ mock topic {}", topic);
        }
    }
}
