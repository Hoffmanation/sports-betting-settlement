package com.sportsbetting.application.port;

import com.sportsbetting.domain.model.BetSettlementMessage;

public interface BetSettlementPublisher {

    void publish(BetSettlementMessage message);
}
