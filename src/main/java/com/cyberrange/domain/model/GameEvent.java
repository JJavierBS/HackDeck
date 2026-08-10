package com.cyberrange.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Entrada del registro de eventos generado al resolver una ronda.
 *
 * "actor" es el bando que lo provoco y "visibleToDefender" modela que solo
 * las acciones detectadas (por ruido, IDS o revision de logs) aparecen para
 * el defensor. Cada bando ve siempre lo suyo; el instructor lo ve todo.
 */
public record GameEvent(
        int roundNumber,
        Role actor,
        String description,
        boolean visibleToDefender,
        Instant occurredAt) {

    public GameEvent {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public boolean isVisibleTo(Role side) {
        return actor == side || (side == Role.DEFENDER && visibleToDefender);
    }
}
