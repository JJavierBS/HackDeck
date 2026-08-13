package com.cyberdeck.application.view;

import java.util.List;
import java.util.Map;

/**
 * El torneo entero para el instructor: una fila por mesa con lo justo para
 * gobernarla, mas la clasificacion.
 *
 * Cada mesa viaja con su propio token porque el del torneo no vale para
 * mandar en una partida concreta.
 */
public record TournamentView(
        String tournamentId,
        String joinCode,
        String phase,
        int roundNumber,
        boolean roundComplete,
        String championName,
        List<TeamStanding> standings,
        List<TableView> tables) {

    public record TeamStanding(String teamId, String displayName, String status, int wins, int defendedCia) {
    }

    public record TableView(
            String gameId,
            String instructorToken,
            String homeName,
            String awayName,
            String phase,
            Integer halfNumber,
            int roundNumber,
            Map<String, Integer> ciaLevels,
            List<String> readyTeams,
            String winnerName) {
    }
}
