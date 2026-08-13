package com.cyberrange.application.view;

import java.util.Map;

/**
 * Una linea del registro. ciaAfter solo viene en el cierre de ronda, y es
 * lo que permite rebobinar el match en el debriefing; detail solo en las
 * acciones, y es lo que se le cuenta a quien las jugo.
 */
public record EventView(
        int halfNumber,
        int roundNumber,
        String type,
        String actor,
        String cardId,
        String description,
        Map<String, Integer> ciaAfter,
        EventDetailView detail,
        String occurredAt) {
}
