package com.sportsbetting.domain.exception;

public class EventOutcomeConflictException extends RuntimeException {

    public EventOutcomeConflictException(String eventId, String existingWinnerId, String requestedWinnerId) {
        super("Conflicting outcome for event %s: winner is already %s, not %s"
            .formatted(eventId, existingWinnerId, requestedWinnerId));
    }
}
