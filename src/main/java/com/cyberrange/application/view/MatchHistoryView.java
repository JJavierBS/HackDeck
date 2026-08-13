package com.cyberrange.application.view;

import java.util.List;

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
