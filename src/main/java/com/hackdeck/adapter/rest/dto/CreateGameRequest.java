package com.hackdeck.adapter.rest.dto;

import com.hackdeck.domain.model.GameSettings;

import java.time.Duration;

public record CreateGameRequest(
        Integer roundsPerHalf,
        Integer roundTimeoutSeconds,
        Integer initialBudget,
        Integer incomePerRound) {

    public GameSettings toSettings() {
        GameSettings defaults = GameSettings.DEFAULTS;
        return new GameSettings(
                roundsPerHalf == null ? defaults.roundsPerHalf() : roundsPerHalf,
                roundTimeoutSeconds == null ? defaults.roundTimeout() : Duration.ofSeconds(roundTimeoutSeconds),
                initialBudget == null ? defaults.initialBudget() : initialBudget,
                incomePerRound == null ? defaults.incomePerRound() : incomePerRound);
    }
}
