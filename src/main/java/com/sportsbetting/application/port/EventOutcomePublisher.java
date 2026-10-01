package com.sportsbetting.application.port;

import com.sportsbetting.domain.model.EventOutcome;

public interface EventOutcomePublisher {

    void publish(EventOutcome outcome);
}
