package com.sportsbetting.ports.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    @Version
    @Column(nullable = false)
    private Long version;
}
