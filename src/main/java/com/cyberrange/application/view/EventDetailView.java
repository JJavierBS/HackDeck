package com.cyberrange.application.view;

import java.util.List;
import java.util.Map;

public record EventDetailView(
        Boolean success,
        String failureReason,
        Map<String, Integer> impact,
        int mitigated,
        List<String> unlocked,
        List<String> boosts,
        Boolean detected,
        String counteredBy) {
}
