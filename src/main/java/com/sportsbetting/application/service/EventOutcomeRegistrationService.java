package com.sportsbetting.application.service;

import com.sportsbetting.domain.exception.EventOutcomeConflictException;
import com.sportsbetting.domain.mapper.BetMapper;
import com.sportsbetting.domain.model.EventOutcome;
import com.sportsbetting.ports.jpa.entity.EventOutcomeEntity;
import com.sportsbetting.ports.jpa.repository.EventOutcomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventOutcomeRegistrationService {

    private final EventOutcomeRepository eventOutcomeRepository;
    private final BetMapper betMapper;

    // Idempotent persistence: if the outcome is already present, it checks if the winner is the same.
    // If not, it throws an exception. If the outcome is not present, it saves it.
    public boolean persistIfAbsent(EventOutcome outcome) {
        return findExisting(outcome)
            .map(existing -> ensureSameWinner(outcome, existing))
            .orElseGet(() -> insertOrResolveRace(outcome));
    }

    private boolean insertOrResolveRace(EventOutcome outcome) {
        try {
            var eventOutcomeEntity = betMapper.toEntity(outcome);
            eventOutcomeRepository.saveAndFlush(eventOutcomeEntity);
            return true;
        }
        // In case of a race condition where another thread has inserted the same event outcome,
        // we catch the exception and check if the winner is the same.
        catch (DataIntegrityViolationException exception) {
            return findExisting(outcome)
                .map(existing -> ensureSameWinner(outcome, existing))
                .orElseThrow(() -> exception);
        }
    }

    private Optional<EventOutcomeEntity> findExisting(EventOutcome outcome) {
        return eventOutcomeRepository.findByEventId(outcome.eventId());
    }

    private boolean ensureSameWinner(EventOutcome outcome, EventOutcomeEntity existing) {
        if (!existing.getWinnerId().trim().equals(outcome.winnerId().trim())) {
            throw new EventOutcomeConflictException(outcome.eventId(), existing.getWinnerId(), outcome.winnerId());
        }
        return false;
    }
}
