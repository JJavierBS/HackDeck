package com.hackdeck.application.view;

import java.util.Map;

public record QueuedActionView(String intentId, String cardId, Map<String, String> parameters) {
}
