package com.cyberrange.application.view;

import java.util.Map;

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
