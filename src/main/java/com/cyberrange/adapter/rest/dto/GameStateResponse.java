package com.cyberrange.adapter.rest.dto;

import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.Participant;
import com.cyberrange.domain.model.TeamId;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Vista de la partida que devuelve el REST. Hoy es la misma para todos los
 * roles; la niebla de guerra (payload distinto para atacante y defensor)
 * llega en la fase 5 del roadmap.
 */
// TODO: Fase 5 del roadmap, filtrar esta respuesta por rol.
public record GameStateResponse(
        String gameId,
        String joinCode,
        String phase,
        Map<String, Integer> ciaLevels,
        int currentRoundNumber,
        Map<String, String> teams,
        String yourTeam,
        String yourSide) {

    public static GameStateResponse from(Game game, Participant viewer) {
        Map<String, Integer> ciaLevels = new LinkedHashMap<>();
        for (CiaPillar pillar : CiaPillar.values()) {
            ciaLevels.put(pillar.name(), game.ciaState().levelOf(pillar));
        }
        Map<String, String> teams = new LinkedHashMap<>();
        for (TeamId team : TeamId.values()) {
            game.playerOf(team).ifPresent(player -> teams.put(team.name(), player.displayName()));
        }
        TeamId viewerTeam = viewer.team();
        return new GameStateResponse(
                game.id().toString(),
                game.joinCode().toString(),
                game.phase().name(),
                ciaLevels,
                game.rounds().size(),
                teams,
                viewerTeam == null ? null : viewerTeam.name(),
                viewerTeam == null ? null : game.sideOf(viewerTeam).name());
    }
}
