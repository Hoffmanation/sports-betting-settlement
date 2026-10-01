package com.sportsbetting.application.service;

import com.sportsbetting.application.port.BetCommandPublisher;
import com.sportsbetting.domain.event.BetCommand;
import com.sportsbetting.domain.exception.BetNotFoundException;
import com.sportsbetting.domain.mapper.BetMapper;
import com.sportsbetting.domain.model.Bet;
import com.sportsbetting.domain.model.BetCommandAccepted;
import com.sportsbetting.domain.model.BetSlice;
import com.sportsbetting.ports.jpa.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetApplicationService {

    private static final Sort BET_ORDER = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final BetRepository betRepository;
    private final BetMapper betMapper;
    private final BetCommandPublisher betCommandPublisher;

    public BetCommandAccepted requestBetCreation(Bet bet) {
        var command = BetCommand.create(bet);
        betCommandPublisher.publish(command);
        log.info(
            "Accepted Kafka bet command {} for bet {} and event {}",
            command.commandId(),
            bet.id(),
            bet.eventId()
        );
        return BetCommandAccepted.builder()
            .commandId(command.commandId())
            .betId(bet.id())
            .commandType(command.commandType())
            .acceptedAt(command.occurredAt())
            .build();
    }

    @Transactional
    @CacheEvict(cacheNames = "betsSearch", allEntries = true)
    public Bet persistBetFromCommand(Bet bet) {
        if (betRepository.existsById(bet.id())) {
            log.info("Ignoring duplicate bet creation command for bet {}", bet.id());
            return betRepository.findById(bet.id())
                .map(betMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("Bet disappeared after duplicate command check"));
        }

        var createdBet = betMapper.toDomain(betRepository.saveAndFlush(betMapper.toEntity(bet)));
        log.info("Created bet {} for event {}", createdBet.id(), createdBet.eventId());
        return createdBet;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "betsById", key = "#betId")
    public Bet getBet(UUID betId) {
        log.trace("Cache miss for bet {}", betId);
        return betRepository.findById(betId)
            .map(betMapper::toDomain)
            .orElseThrow(() -> new BetNotFoundException(betId));
    }

    @Transactional(readOnly = true)
    @Cacheable(
        cacheNames = "betsSearch",
        key = "(#eventId ?: 'all') + '|' + #page + '|' + #size"
    )
    public BetSlice getBets(String eventId, int page, int size) {
        log.info("Cache miss for bet search. eventId={}, page={}, size={}", eventId, page, size);
        var pageable = PageRequest.of(page, size, BET_ORDER);
        var bets = eventId == null
            ? betRepository.findAllBets(pageable)
            : betRepository.findAllByEventId(eventId, pageable);
        Slice<Bet> domainBets = bets.map(betMapper::toDomain);
        return BetSlice.builder()
            .content(domainBets.getContent())
            .page(domainBets.getNumber())
            .size(domainBets.getSize())
            .first(domainBets.isFirst())
            .hasNext(domainBets.hasNext())
            .build();
    }
}
