package com.cyberrange.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Una ronda de la partida: las acciones que ambos bandos han encolado. Lo
 * que ocurre al resolverla no se guarda aqui sino en el historial del
 * match, que es una unica linea temporal ordenada.
 */
public final class Round {

    private final int number;
    private final List<ActionIntent> queuedActions = new ArrayList<>();

    public Round(int number) {
        this.number = number;
    }

    public int number() {
        return number;
    }

    public List<ActionIntent> queuedActions() {
        return List.copyOf(queuedActions);
    }

    /**
     * Solo guarda la accion: validar presupuesto y coste es regla de juego
     * y lo hace el motor de reglas antes de llamar aqui.
     */
    public void enqueue(ActionIntent action) {
        queuedActions.add(Objects.requireNonNull(action, "action"));
    }
}
