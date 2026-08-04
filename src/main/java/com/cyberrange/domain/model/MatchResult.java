package com.cyberrange.domain.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Resultado final del match.
 *
 * @param winner        equipo ganador, o vacio si hubo empate.
 * @param outcome       motivo de la victoria.
 * @param defendedCia   triada que cada equipo consiguio defender en su mitad.
 * @param takedownRound ronda en la que cada equipo derribo un pilar atacando.
 */
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
