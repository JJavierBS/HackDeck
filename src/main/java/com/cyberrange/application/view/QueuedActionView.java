package com.cyberrange.application.view;

import java.util.Map;

public record QueuedActionView(String actionType, Map<String, String> parameters, boolean noisy) {
}
