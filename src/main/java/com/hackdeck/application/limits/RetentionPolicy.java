package com.hackdeck.application.limits;

import com.hackdeck.domain.model.GamePhase;
import com.hackdeck.domain.model.TournamentPhase;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Cuando una partida deja de merecer memoria. No es una regla de juego (no
 * cambia quien gana), sino la politica de un servidor que atiende a gente
 * que no conoce: por eso vive en la capa de aplicacion y no en el motor.
 *
 * El plazo depende de en que fase se quedo: un lobby vacio no le importa a
 * nadie a los treinta minutos, mientras que una partida terminada aun sirve
 * para el debriefing en clase.
 */
public final class RetentionPolicy {

    private final Duration lobbyTtl;
    private final Duration idleTtl;
    private final Duration finishedTtl;
    private final int maxLiveGames;
    private final int maxLiveTournaments;

    public RetentionPolicy(
            Duration lobbyTtl,
            Duration idleTtl,
            Duration finishedTtl,
            int maxLiveGames,
            int maxLiveTournaments) {
        this.lobbyTtl = Objects.requireNonNull(lobbyTtl, "lobbyTtl");
        this.idleTtl = Objects.requireNonNull(idleTtl, "idleTtl");
        this.finishedTtl = Objects.requireNonNull(finishedTtl, "finishedTtl");
        this.maxLiveGames = maxLiveGames;
        this.maxLiveTournaments = maxLiveTournaments;
    }

    public boolean isPurgeable(GamePhase phase, Instant lastActivity, Instant now) {
        return hasExpired(lastActivity, now, ttlFor(phase));
    }

    /**
     * Un torneo se borra por su propia inactividad, pero sus mesas caducan
     * cada una por su cuenta: el barrido de partidas ya las recoge.
     */
    public boolean isPurgeable(TournamentPhase phase, Instant lastActivity, Instant now) {
        return hasExpired(lastActivity, now, switch (phase) {
            case LOBBY -> lobbyTtl;
            case IN_PROGRESS -> idleTtl;
            case FINISHED -> finishedTtl;
        });
    }

    public boolean gamesAreFull(int liveGames) {
        return liveGames >= maxLiveGames;
    }

    /**
     * Un torneo siembra todas las mesas de la ronda de una vez: o caben todas
     * o no arranca ninguna.
     */
    public boolean hasRoomForGames(int liveGames, int extraGames) {
        return liveGames + extraGames <= maxLiveGames;
    }

    public boolean tournamentsAreFull(int liveTournaments) {
        return liveTournaments >= maxLiveTournaments;
    }

    public int maxLiveGames() {
        return maxLiveGames;
    }

    public int maxLiveTournaments() {
        return maxLiveTournaments;
    }

    private Duration ttlFor(GamePhase phase) {
        return switch (phase) {
            case PREPARATION -> lobbyTtl;
            case IN_PROGRESS -> idleTtl;
            case FINISHED -> finishedTtl;
        };
    }

    private static boolean hasExpired(Instant lastActivity, Instant now, Duration ttl) {
        return lastActivity != null && Duration.between(lastActivity, now).compareTo(ttl) > 0;
    }
}
