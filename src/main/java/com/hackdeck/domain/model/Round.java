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

    public ActionIntent findAction(java.util.UUID intentId, Role side) {
        return queuedActions.stream()
                .filter(action -> action.id().equals(intentId) && action.team() == side)
                .findFirst()
                .orElse(null);
    }

    public boolean removeAction(java.util.UUID intentId, Role side) {
        return queuedActions.removeIf(action -> action.id().equals(intentId) && action.team() == side);
    }

    public void reorderQueue(Role side, List<java.util.UUID> intentIds) {
        List<ActionIntent> sideActions = new ArrayList<>(
                queuedActions.stream().filter(a -> a.team() == side).toList()
        );
        java.util.Map<java.util.UUID, ActionIntent> byId = new java.util.HashMap<>();
        sideActions.forEach(a -> byId.put(a.id(), a));

        List<ActionIntent> reordered = new ArrayList<>();
        for (java.util.UUID id : intentIds) {
            ActionIntent action = byId.remove(id);
            if (action != null) {
                reordered.add(action);
            }
        }
        reordered.addAll(byId.values());

        List<ActionIntent> result = new ArrayList<>();
        int reorderedIdx = 0;
        for (ActionIntent action : queuedActions) {
            if (action.team() == side) {
                if (reorderedIdx < reordered.size()) {
                    result.add(reordered.get(reorderedIdx++));
                }
            } else {
                result.add(action);
            }
        }
        queuedActions.clear();
        queuedActions.addAll(result);
    }
}
