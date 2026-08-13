package com.hackdeck.adapter.rest.dto;

import java.util.Map;

public record EnqueueActionRequest(String cardId, Map<String, String> parameters) {
}
