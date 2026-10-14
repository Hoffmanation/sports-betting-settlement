package com.sportsbetting.ports.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "event_outcomes", uniqueConstraints = {
    @UniqueConstraint(name = "uk_event_outcomes_event_id", columnNames = "event_id")
})
public class EventOutcomeEntity extends BaseEntity {

    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "winner_id", nullable = false)
    private String winnerId;
}
