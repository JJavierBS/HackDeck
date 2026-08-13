package com.hackdeck.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Lo que ocurre al resolverla no se guarda aqui sino en Game.history. */
public final class Round {

    private final int number;
    private final Instant startedAt;
    private final List<ActionIntent> queuedActions = new ArrayList<>();
    private final Set<TeamId> readyTeams = EnumSet.noneOf(TeamId.class);

    public Round(int number) {
        this.number = number;
        this.startedAt = Instant.now();
    }

    public int number() {
        return number;
    }

    public Instant startedAt() {
        return startedAt;
    }

    /**
     * Un equipo declara que ya ha decidido. No cierra la ronda por si solo:
     * eso depende de si el instructor tiene puesto el modo automatico.
     */
    public void markReady(TeamId team) {
        readyTeams.add(team);
    }

    public Set<TeamId> readyTeams() {
        return Set.copyOf(readyTeams);
    }

    public boolean everyoneReady() {
        return readyTeams.size() >= TeamId.values().length;
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
