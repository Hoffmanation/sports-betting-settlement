package com.sportsbetting.ports.jpa.repository;

import com.sportsbetting.ports.jpa.entity.EventOutcomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventOutcomeRepository extends JpaRepository<EventOutcomeEntity, UUID> {

    Optional<EventOutcomeEntity> findByEventId(String eventId);
}
