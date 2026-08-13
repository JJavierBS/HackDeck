package com.hackdeck.domain.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record MatchResult(
        TeamId winner,
        MatchOutcome outcome,
        Map<TeamId, Integer> defendedCia,
        Map<TeamId, Integer> takedownRound) {

    public MatchResult {
        Objects.requireNonNull(outcome, "outcome");
        defendedCia = Collections.unmodifiableMap(new EnumMap<>(defendedCia));
        takedownRound = Collections.unmodifiableMap(new EnumMap<>(takedownRound));
    }
}
