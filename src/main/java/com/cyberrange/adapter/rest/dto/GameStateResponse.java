package com.cyberrange.adapter.rest.dto;

import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.CiaState;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GamePhase;
import com.cyberrange.domain.model.Half;
import com.cyberrange.domain.model.Participant;
import com.cyberrange.domain.model.TeamId;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Vista de la partida que devuelve el REST.
 *
 * Del presupuesto solo se manda el propio: saber cuanto tiene ahorrado el
 * rival delataria si prepara algo caro. El instructor los ve todos.
 */
// TODO: Fase 5 del roadmap, filtrar tambien la triada y los eventos por rol.
public record GameStateResponse(
        String gameId,
        String joinCode,
        String phase,
        Map<String, Integer> ciaLevels,
        Integer halfNumber,
        int currentRoundNumber,
        int roundsPerHalf,
        long roundTimeoutSeconds,
        Map<String, String> teams,
        String yourTeam,
        String yourSide,
        Integer yourBudget,
        Map<String, Integer> budgets,
        MatchResultResponse result) {

    public static GameStateResponse from(Game game, Participant viewer) {
        boolean started = game.phase() != GamePhase.PREPARATION;
        Half half = started ? game.currentHalf() : null;
        TeamId viewerTeam = viewer.team();

        Map<String, Integer> ciaLevels = new LinkedHashMap<>();
        for (CiaPillar pillar : CiaPillar.values()) {
            ciaLevels.put(pillar.name(), half == null ? CiaState.MAX_LEVEL : half.ciaState().levelOf(pillar));
        }

        Map<String, String> teams = new LinkedHashMap<>();
        for (TeamId team : TeamId.values()) {
            game.playerOf(team).ifPresent(player -> teams.put(team.name(), player.displayName()));
        }

        return new GameStateResponse(
                game.id().toString(),
                game.joinCode().toString(),
                game.phase().name(),
                ciaLevels,
                half == null ? null : half.number(),
                half == null ? 0 : half.currentRound().number(),
                game.settings().roundsPerHalf(),
                game.settings().roundTimeout().toSeconds(),
                teams,
                viewerTeam == null ? null : viewerTeam.name(),
                viewerTeam == null || half == null ? null : game.sideOf(viewerTeam).name(),
                viewerTeam == null || half == null ? null : half.budgetOf(viewerTeam),
                viewer.isInstructor() && half != null ? budgetsByTeamName(half) : null,
                game.result() == null ? null : MatchResultResponse.from(game.result()));
    }

    private static Map<String, Integer> budgetsByTeamName(Half half) {
        Map<String, Integer> budgets = new LinkedHashMap<>();
        half.budgets().forEach((team, budget) -> budgets.put(team.name(), budget));
        return budgets;
    }
}
