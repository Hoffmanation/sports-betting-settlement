package com.sportsbetting.ports.jpa.repository;

import com.sportsbetting.domain.model.BetStatus;
import com.sportsbetting.ports.jpa.entity.BetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface BetRepository extends JpaRepository<BetEntity, UUID> {

    @Query("select bet from BetEntity bet")
    Slice<BetEntity> findAllBets(Pageable pageable);

    Slice<BetEntity> findAllByEventId(String eventId, Pageable pageable);

    Slice<BetEntity> findAllByEventIdAndStatus(String eventId, BetStatus status, Pageable pageable);

    @Query("""
        select bet from BetEntity bet
        where bet.eventId = :eventId
          and bet.status = :status
          and (bet.createdAt > :afterCreatedAt
            or (bet.createdAt = :afterCreatedAt and bet.id > :afterId))
        """)
    Slice<BetEntity> findNextOpenBets(
        @Param("eventId") String eventId,
        @Param("status") BetStatus status,
        @Param("afterCreatedAt") Instant afterCreatedAt,
        @Param("afterId") UUID afterId,
        Pageable pageable
    );

    @Modifying
    @Query("""
        update BetEntity bet
        set bet.status = :newStatus, bet.settledAt = :settledAt, bet.version = bet.version + 1
        where bet.id = :betId and bet.status = :expectedStatus
        """)
    int settleIfOpen(
        @Param("betId") UUID betId,
        @Param("expectedStatus") BetStatus expectedStatus,
        @Param("newStatus") BetStatus newStatus,
        @Param("settledAt") Instant settledAt
    );
}
