package com.cyberrange.domain.rules;

import com.cyberrange.domain.model.CiaState;
import com.cyberrange.domain.model.GameEvent;
import com.cyberrange.domain.model.TeamId;

import java.util.List;
import java.util.Map;

/**
 * Resultado de resolver una ronda: nuevo estado de la triada, eventos
 * generados, si en ella cayo un pilar y el presupuesto extra que el motor
 * concede al que va perdiendo. Quien gana el match no se decide aqui sino
 * al puntuarlo, porque un derribo puede quedar empatado si el rival tambien
 * derriba en su mitad.
 */
public record RoundResolution(
        CiaState resultingState,
        List<GameEvent> generatedEvents,
        boolean takedown,
        Map<TeamId, Integer> catchUpBonus) {

    public RoundResolution {
        generatedEvents = generatedEvents == null ? List.of() : List.copyOf(generatedEvents);
        catchUpBonus = catchUpBonus == null ? Map.of() : Map.copyOf(catchUpBonus);
    }
}
