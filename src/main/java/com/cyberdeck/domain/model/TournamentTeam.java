package com.cyberdeck.domain.model;

import java.util.Objects;

/**
 * Un equipo del torneo. Su identidad dura todo el torneo aunque cambie de
 * mesa y de bando en cada emparejamiento.
 */
public record TournamentTeam(PlayerId id, String displayName) {

    public TournamentTeam {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
    }
}
