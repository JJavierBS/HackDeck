package com.hackdeck.application.view;

import java.util.Map;

public record QueuedActionView(String cardId, Map<String, String> parameters) {
}
