package com.cyberrange.domain.rules;

import com.cyberrange.domain.model.CiaState;
import com.cyberrange.domain.model.GameEvent;

import java.util.List;

/**
 * Resultado de resolver una ronda: nuevo estado de la triada, eventos
 * generados y si en ella cayo un pilar. Quien gana el match no se decide
 * aqui sino al puntuarlo, porque un derribo puede quedar empatado si el
 * rival tambien derriba en su mitad.
 */
public record RoundResolution(CiaState resultingState, List<GameEvent> generatedEvents, boolean takedown) {

    public RoundResolution {
        generatedEvents = generatedEvents == null ? List.of() : List.copyOf(generatedEvents);
    }
}
