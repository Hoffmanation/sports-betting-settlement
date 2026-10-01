package com.sportsbetting.integration.rocketmq;

import com.sportsbetting.domain.model.BetSettlementMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class MockRocketMqBroker {

    private final BlockingQueue<BetSettlementMessage> queue = new LinkedBlockingQueue<>();

    public void send(BetSettlementMessage message) {
        queue.add(message);
        log.debug("Queued settlement message for bet {}", message.betId());
    }

    public BetSettlementMessage peek() {
        return queue.peek();
    }

    public boolean acknowledge(BetSettlementMessage message) {
        return queue.remove(message);
    }
}
