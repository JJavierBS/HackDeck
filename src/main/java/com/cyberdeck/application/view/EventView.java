package com.cyberdeck.application.view;

import java.util.Map;

public record EventView(
        int halfNumber,
        int roundNumber,
        String type,
        String actor,
        String cardId,
        Map<String, String> cardName,
        String description,
        Map<String, Integer> ciaAfter,
        EventDetailView detail,
        String occurredAt) {
}
