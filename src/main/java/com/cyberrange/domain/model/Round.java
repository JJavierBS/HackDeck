package com.cyberrange.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Una ronda de la partida: acciones encoladas por ambos bandos y eventos
 * generados al resolverla.
 */
public final class Round {

    private final int number;
    private final List<ActionIntent> queuedActions = new ArrayList<>();
    private final List<GameEvent> events = new ArrayList<>();

    public Round(int number) {
        this.number = number;
    }

    public int number() {
        return number;
    }

    public List<ActionIntent> queuedActions() {
        return List.copyOf(queuedActions);
    }

    public List<GameEvent> events() {
        return List.copyOf(events);
    }

    /**
     * Solo guarda la accion: validar presupuesto y coste es regla de juego
     * y lo hara el motor de reglas antes de llamar aqui.
     */
    // TODO: Fase 3 del roadmap, validacion de presupuesto en RuleEngine.
    public void enqueue(ActionIntent action) {
        queuedActions.add(Objects.requireNonNull(action, "action"));
    }

    public void recordEvent(GameEvent event) {
        events.add(Objects.requireNonNull(event, "event"));
    }
}
