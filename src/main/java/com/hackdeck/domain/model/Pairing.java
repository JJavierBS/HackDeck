package com.hackdeck.domain.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Un cruce de la ronda. Con rival vacio el equipo pasa directo, que es lo
 * que ocurre cuando el numero de equipos es impar.
 */
public final class Pairing {

    private final PlayerId home;
    private final PlayerId away;
    private GameId gameId;
    private PlayerId winner;

    public Pairing(PlayerId home, PlayerId away) {
        this.home = Objects.requireNonNull(home, "home");
        this.away = away;
        if (away == null) {
            this.winner = home;
        }
    }

    public PlayerId home() {
        return home;
    }

    public Optional<PlayerId> away() {
        return Optional.ofNullable(away);
    }

    public boolean isBye() {
        return away == null;
    }

    public Optional<GameId> gameId() {
        return Optional.ofNullable(gameId);
    }

    public void playedIn(GameId game) {
        this.gameId = game;
    }

    public Optional<PlayerId> winner() {
        return Optional.ofNullable(winner);
    }

    public void resolvedBy(PlayerId ganador) {
        this.winner = ganador;
    }

    public boolean involves(PlayerId team) {
        return home.equals(team) || team.equals(away);
    }
}
