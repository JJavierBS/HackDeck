package com.cyberrange.application.view;

import java.util.List;

/**
 * Todo lo que hace falta para reconstruir el match en clase: quien jugaba,
 * como acabo y la linea temporal completa, sin filtrar por rol. Es del
 * instructor: al terminar ya no hay niebla de guerra que proteger.
 *
 * @param settings rondas por mitad y tiempo, para poder leer el ritmo.
 */
public record MatchHistoryView(
        String gameId,
        String joinCode,
        String phase,
        Settings settings,
        java.util.Map<String, String> teams,
        List<EventView> events,
        MatchResultView result) {

    public record Settings(int roundsPerHalf, long roundTimeoutSeconds, int initialBudget, int incomePerRound) {
    }
}
