package com.cyberrange.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Entrada del historial de la partida. Se guarda pensando en reproducir el
 * match despues con los alumnos, asi que lleva de que ronda y mitad es, que
 * clase de suceso fue y que carta lo provoco, ademas del texto.
 *
 * "actor" es el bando responsable, o vacio si es un suceso de la partida que
 * ven todos. "visibleToDefender" modela que solo las acciones detectadas
 * aparecen para el defensor mientras se juega; el historial completo es del
 * instructor.
 *
 * @param ciaAfter foto de la triada al cerrar la ronda, solo en
 *                 ROUND_RESOLVED. Es lo que permite rebobinar la partida sin
 *                 tener que recalcularla.
 */
public record GameEvent(
        int halfNumber,
        int roundNumber,
        GameEventType type,
        Role actor,
        String cardId,
        String description,
        boolean visibleToDefender,
        Map<CiaPillar, Integer> ciaAfter,
        Instant occurredAt) {

    public GameEvent {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(occurredAt, "occurredAt");
        ciaAfter = ciaAfter == null ? Map.of() : Map.copyOf(ciaAfter);
    }

    public static GameEvent of(
            int halfNumber, int roundNumber, GameEventType type, String description) {
        return new GameEvent(halfNumber, roundNumber, type, null, null, description, true, Map.of(), Instant.now());
    }

    public static GameEvent byCard(
            int halfNumber,
            int roundNumber,
            GameEventType type,
            Role actor,
            String cardId,
            String description,
            boolean visibleToDefender) {
        return new GameEvent(
                halfNumber, roundNumber, type, actor, cardId, description, visibleToDefender, Map.of(), Instant.now());
    }

    /**
     * Los sucesos de partida los ve todo el mundo; los de un bando, ese bando,
     * y el defensor ve ademas lo que haya detectado.
     */
    public boolean isVisibleTo(Role side) {
        if (actor == null) {
            return true;
        }
        return actor == side || (side == Role.DEFENDER && visibleToDefender);
    }
}
