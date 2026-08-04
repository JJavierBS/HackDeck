package com.cyberrange.adapter.rest.dto;

import com.cyberrange.domain.model.GameSettings;

import java.time.Duration;

/**
 * Configuracion que el instructor elige al crear la partida. Todo campo
 * omitido cae al valor por defecto.
 */
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
