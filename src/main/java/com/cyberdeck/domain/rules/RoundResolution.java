package com.cyberdeck.domain.rules;

import com.cyberdeck.domain.model.CiaState;
import com.cyberdeck.domain.model.GameEvent;
import com.cyberdeck.domain.model.TeamId;

import java.util.List;
import java.util.Map;

public record RoundResolution(
        CiaState resultingState,
        List<GameEvent> generatedEvents,
        boolean takedown,
        Map<TeamId, Integer> catchUpBonus,
        boolean revealsPreviousRound) {

    public RoundResolution {
        generatedEvents = generatedEvents == null ? List.of() : List.copyOf(generatedEvents);
        catchUpBonus = catchUpBonus == null ? Map.of() : Map.copyOf(catchUpBonus);
    }
}
