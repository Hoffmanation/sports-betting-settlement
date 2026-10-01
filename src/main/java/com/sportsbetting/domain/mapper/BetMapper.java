package com.sportsbetting.domain.mapper;

import com.sportsbetting.api.model.BetResponse;
import com.sportsbetting.api.model.BetCommandAcceptedResponse;
import com.sportsbetting.api.model.CreateBetRequest;
import com.sportsbetting.api.model.CreateBetResponse;
import com.sportsbetting.api.model.PublishEventOutcomeRequest;
import com.sportsbetting.domain.model.Bet;
import com.sportsbetting.domain.model.BetCommandAccepted;
import com.sportsbetting.domain.model.BetStatus;
import com.sportsbetting.domain.model.EventOutcome;
import com.sportsbetting.ports.jpa.entity.BetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(imports = {UUID.class, BetStatus.class})
public interface BetMapper {

    @Mapping(target = "betId", source = "id")
    BetResponse toResponse(Bet bet);

    @Mapping(target = "betId", source = "id")
    CreateBetResponse toCreateResponse(Bet bet);

    BetCommandAcceptedResponse toCommandAcceptedResponse(BetCommandAccepted accepted);

    Bet toDomain(BetEntity entity);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "status", expression = "java(BetStatus.OPEN)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "settledAt", ignore = true)
    Bet toDomain(CreateBetRequest request);

    @Mapping(target = "version", ignore = true)
    BetEntity toEntity(Bet bet);

    EventOutcome toDomain(PublishEventOutcomeRequest request);
}
