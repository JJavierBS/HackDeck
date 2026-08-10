package com.cyberrange.domain.rules;

/**
 * Fuente de azar del juego. Se aisla tras una interfaz para poder inyectar
 * una fija en los tests; en partida no se exige reproducibilidad.
 */
public interface Randomizer {

    /**
     * @return true con la probabilidad indicada (0 nunca, 1 siempre).
     */
    boolean chance(double probability);
}
