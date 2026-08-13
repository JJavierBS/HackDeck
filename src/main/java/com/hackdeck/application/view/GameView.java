package com.hackdeck.application.view;

import java.util.List;
import java.util.Map;

/**
 * Lo que un participante concreto puede ver. Los campos que su rol no debe
 * conocer viajan a null: no es que el cliente los oculte, es que nunca salen
 * del servidor.
 */
public record GameView(
        String gameId,
        String joinCode,
        String phase,
        Integer halfNumber,
        int currentRoundNumber,
        int roundsPerHalf,
        long roundTimeoutSeconds,
        Map<String, String> teams,
        String yourTeam,
        String yourSide,
        Integer yourBudget,
        Map<String, Integer> budgets,
        Map<String, Integer> ciaLevels,
        String attackingTeam,
        String roundDeadlineAt,
        Boolean autoResolve,
        List<String> readyTeams,
        Map<String, List<QueuedActionView>> queuedBySide,
        List<String> yourKillChain,
        List<ActiveCardView> yourActiveCards,
        List<ActiveCardView> revealedRivalCards,
        List<QueuedActionView> yourQueuedActions,
        List<EventView> events,
        MatchResultView result) {
}
