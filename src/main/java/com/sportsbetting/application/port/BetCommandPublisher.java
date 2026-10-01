package com.sportsbetting.application.port;

import com.sportsbetting.domain.event.BetCommand;

public interface BetCommandPublisher {

    void publish(BetCommand command);
}
