package com.cyberrange.adapter.rest.dto;

import java.util.Map;

public record EnqueueActionRequest(
        String actionType,
        Map<String, String> parameters,
        boolean noisy) {
}
