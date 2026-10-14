package com.sportsbetting.ports.jpa.entity;

import com.sportsbetting.domain.model.BetStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bets", indexes = {
    @Index(name = "idx_bets_event_status_created", columnList = "event_id,status,created_at,id")
})
public class BetEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "event_market_id", nullable = false)
    private String eventMarketId;

    @Column(name = "selected_winner_id", nullable = false)
    private String selectedWinnerId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BetStatus status;

    @Column(name = "settled_at")
    private Instant settledAt;
}
