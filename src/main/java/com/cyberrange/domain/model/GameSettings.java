package com.cyberrange.domain.model;

import java.time.Duration;

/**
 * Parametros que el instructor fija al crear la partida. Se eligen una vez
 * y no cambian durante el match, para que todas las mesas de una liga
 * jueguen en las mismas condiciones.
 *
 * @param roundsPerHalf  rondas de cada mitad; ambas duran lo mismo en jugadas.
 * @param roundTimeout   tiempo por ronda para decidir. De momento es
 *                       informativo: el temporizador del servidor es fase 6.
 * @param initialBudget  presupuesto con el que arranca cada bando cada mitad.
 * @param incomePerRound ingreso al empezar cada ronda posterior a la primera.
 */
public record GameSettings(int roundsPerHalf, Duration roundTimeout, int initialBudget, int incomePerRound) {

    private static final int MAX_ROUNDS_PER_HALF = 20;
    private static final Duration MIN_ROUND_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration MAX_ROUND_TIMEOUT = Duration.ofMinutes(10);

    // Se declara despues de los limites: el constructor los valida y en Java
    // los estaticos se inicializan en orden de declaracion.
    public static final GameSettings DEFAULTS =
            new GameSettings(6, Duration.ofSeconds(90), 20, 10);

    public GameSettings {
        if (roundsPerHalf < 1 || roundsPerHalf > MAX_ROUNDS_PER_HALF) {
            throw new IllegalArgumentException("Las rondas por mitad deben estar entre 1 y " + MAX_ROUNDS_PER_HALF);
        }
        if (roundTimeout == null || roundTimeout.compareTo(MIN_ROUND_TIMEOUT) < 0
                || roundTimeout.compareTo(MAX_ROUND_TIMEOUT) > 0) {
            throw new IllegalArgumentException("El tiempo por ronda debe estar entre 15 segundos y 10 minutos");
        }
        if (initialBudget < 0 || incomePerRound < 0) {
            throw new IllegalArgumentException("El presupuesto no puede ser negativo");
        }
    }
}
