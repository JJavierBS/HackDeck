package com.cyberrange.domain.rules;

import com.cyberrange.domain.model.CiaState;
import com.cyberrange.domain.model.GameEvent;
import com.cyberrange.domain.model.TeamId;

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
