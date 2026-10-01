package com.sportsbetting.application.service;

import com.sportsbetting.application.port.BetSettlementPublisher;
import com.sportsbetting.domain.mapper.BetMapper;
import com.sportsbetting.domain.model.Bet;
import com.sportsbetting.domain.model.BetSettlementMessage;
import com.sportsbetting.domain.model.BetStatus;
import com.sportsbetting.domain.model.EventOutcome;
import com.sportsbetting.ports.jpa.entity.BetEntity;
import com.sportsbetting.ports.jpa.repository.BetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventOutcomeProcessingServiceTest {

    @Mock
    private BetRepository betRepository;

    @Mock
    private BetMapper betMapper;

    @Mock
    private BetSettlementPublisher settlementPublisher;

    @InjectMocks
    private EventOutcomeProcessingService service;

    @Test
    void publishesOneSettlementForEachOpenBetOnTheEvent() {
        UUID firstBetId = UUID.randomUUID();
        UUID secondBetId = UUID.randomUUID();
        BetEntity firstEntity = betEntity(firstBetId, "player-a");
        BetEntity secondEntity = betEntity(secondBetId, "player-b");
        when(betRepository.findAllByEventIdAndStatus(eq("match-42"), eq(BetStatus.OPEN), any()))
            .thenReturn(new SliceImpl<>(List.of(firstEntity, secondEntity), PageRequest.of(
                0,
                500,
                Sort.by("createdAt").ascending().and(Sort.by("id").ascending())
            ), false));
        when(betMapper.toDomain(firstEntity)).thenReturn(bet(firstBetId, "player-a"));
        when(betMapper.toDomain(secondEntity)).thenReturn(bet(secondBetId, "player-b"));

        service.process(EventOutcome.builder()
            .eventId("match-42")
            .eventName("Final")
            .winnerId("player-a")
            .build());

        ArgumentCaptor<BetSettlementMessage> messages = ArgumentCaptor.forClass(BetSettlementMessage.class);
        verify(settlementPublisher, times(2)).publish(messages.capture());
        assertThat(messages.getAllValues()).containsExactly(
            BetSettlementMessage.builder()
                .betId(firstBetId)
                .eventId("match-42")
                .selectedWinnerId("player-a")
                .winnerId("player-a")
                .build(),
            BetSettlementMessage.builder()
                .betId(secondBetId)
                .eventId("match-42")
                .selectedWinnerId("player-b")
                .winnerId("player-a")
                .build()
        );
    }

    @Test
    void doesNotPublishSettlementsWhenEventHasNoOpenBets() {
        when(betRepository.findAllByEventIdAndStatus(eq("match-42"), eq(BetStatus.OPEN), any()))
            .thenReturn(new SliceImpl<>(List.of(), PageRequest.of(0, 500), false));

        service.process(EventOutcome.builder()
            .eventId("match-42")
            .eventName("Final")
            .winnerId("player-a")
            .build());

        verify(settlementPublisher, never()).publish(any());
    }

    private static BetEntity betEntity(UUID id, String selectedWinnerId) {
        var entity = new BetEntity();
        entity.setId(id);
        entity.setUserId("user-1");
        entity.setEventId("match-42");
        entity.setEventMarketId("winner-market");
        entity.setSelectedWinnerId(selectedWinnerId);
        entity.setAmount(new BigDecimal("10.00"));
        entity.setStatus(BetStatus.OPEN);
        entity.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        return entity;
    }

    private static Bet bet(UUID id, String selectedWinnerId) {
        return Bet.builder()
            .id(id)
            .userId("user-1")
            .eventId("match-42")
            .eventMarketId("winner-market")
            .selectedWinnerId(selectedWinnerId)
            .amount(new BigDecimal("10.00"))
            .status(BetStatus.OPEN)
            .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
            .build();
    }
}
