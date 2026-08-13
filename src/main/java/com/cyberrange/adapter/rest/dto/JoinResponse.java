package com.cyberrange.adapter.rest.dto;

import com.cyberrange.application.port.in.GameAccess;
import com.cyberrange.application.port.in.TournamentAccess;

/**
 * El equipo teclea un codigo sin saber si es de una partida suelta o de un
 * torneo; el campo kind le dice al cliente que hacer, y el resto del flujo
 * es identico.
 */
public record JoinResponse(
        String kind, String gameId, String tournamentId, String joinCode, String team, String token) {

    public static JoinResponse of(GameAccess access) {
        return new JoinResponse(
                "GAME",
                access.gameId().toString(),
                null,
                access.joinCode().toString(),
                access.participant().team() == null ? null : access.participant().team().name(),
                access.token());
    }

    public static JoinResponse of(TournamentAccess access) {
        return new JoinResponse(
                "TOURNAMENT", null, access.tournamentId().toString(), access.joinCode().toString(), null,
                access.token());
    }
}
