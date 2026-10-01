package com.sportsbetting.domain.model;

import lombok.Builder;

import java.util.List;

@Builder
public record BetSlice(
    List<Bet> content,
    int page,
    int size,
    boolean first,
    boolean hasNext
) {
}
