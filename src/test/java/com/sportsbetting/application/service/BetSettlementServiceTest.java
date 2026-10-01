package com.sportsbetting.application.service;

import com.sportsbetting.domain.model.BetSettlementMessage;
import com.sportsbetting.domain.model.BetStatus;
import com.sportsbetting.ports.jpa.repository.BetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BetSettlementServiceTest {

    @Mock
    private BetRepository betRepository;

    @InjectMocks
    private BetSettlementService service;

    @Test
    void settlesBetAsWonWhenSelectedWinnerMatches() {
        UUID betId = UUID.randomUUID();
        service.settle(BetSettlementMessage.builder()
            .betId(betId)
            .eventId("match-42")
            .selectedWinnerId("player-a")
            .winnerId("player-a")
            .build());

        verify(betRepository).settleIfOpen(
            org.mockito.ArgumentMatchers.eq(betId),
            org.mockito.ArgumentMatchers.eq(BetStatus.OPEN),
            org.mockito.ArgumentMatchers.eq(BetStatus.WON),
            org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void settlesBetAsLostWhenSelectedWinnerDoesNotMatch() {
        UUID betId = UUID.randomUUID();
        service.settle(BetSettlementMessage.builder()
            .betId(betId)
            .eventId("match-42")
            .selectedWinnerId("player-b")
            .winnerId("player-a")
            .build());

        verify(betRepository).settleIfOpen(
            org.mockito.ArgumentMatchers.eq(betId),
            org.mockito.ArgumentMatchers.eq(BetStatus.OPEN),
            org.mockito.ArgumentMatchers.eq(BetStatus.LOST),
            org.mockito.ArgumentMatchers.any()
        );
    }
}
