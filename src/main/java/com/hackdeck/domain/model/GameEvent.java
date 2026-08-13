package com.hackdeck.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Entrada del historial. ciaAfter solo viene en ROUND_RESOLVED y es lo que
 * permite rebobinar el match sin recalcularlo; actor vacio significa suceso
 * de partida, que ven todos.
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
        EventDetail detail,
        Instant occurredAt) {

    public GameEvent {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(occurredAt, "occurredAt");
        ciaAfter = ciaAfter == null ? Map.of() : Map.copyOf(ciaAfter);
    }

    public static GameEvent of(
            int halfNumber, int roundNumber, GameEventType type, String description) {
        return new GameEvent(
                halfNumber, roundNumber, type, null, null, description, true, Map.of(), null, Instant.now());
    }

    public static GameEvent byCard(
            int halfNumber,
            int roundNumber,
            GameEventType type,
            Role actor,
            String cardId,
            String description,
            boolean visibleToDefender,
            EventDetail detail) {
        return new GameEvent(
                halfNumber,
                roundNumber,
                type,
                actor,
                cardId,
                description,
                visibleToDefender,
                Map.of(),
                detail,
                Instant.now());
    }

    public GameEvent revealedToDefender() {
        return new GameEvent(
                halfNumber, roundNumber, type, actor, cardId, description, true, ciaAfter, detail, occurredAt);
    }

    public GameEvent withDetail(EventDetail newDetail) {
        return new GameEvent(
                halfNumber, roundNumber, type, actor, cardId, description, visibleToDefender, ciaAfter, newDetail,
                occurredAt);
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
