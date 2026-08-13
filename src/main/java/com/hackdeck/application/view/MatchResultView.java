package com.hackdeck.application.view;

import java.util.Map;

public record MatchResultView(
        String winner,
        String outcome,
        Map<String, Integer> defendedCia,
        Map<String, Integer> takedownRound) {
}
