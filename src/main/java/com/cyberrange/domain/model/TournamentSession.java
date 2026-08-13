package com.cyberrange.domain.model;

import java.util.Objects;

/**
 * Identidad de un equipo dentro de un torneo. Dura todo el torneo; el token
 * de cada mesa se emite aparte y caduca con ella.
 */
public record TournamentSession(TournamentId tournamentId, PlayerId teamId, String displayName) {

    public TournamentSession {
        Objects.requireNonNull(tournamentId, "tournamentId");
        Objects.requireNonNull(teamId, "teamId");
    }
}
