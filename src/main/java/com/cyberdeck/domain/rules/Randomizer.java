package com.cyberdeck.domain.rules;

/**
 * Fuente de azar del juego. Se aisla tras una interfaz para poder inyectar
 * una fija en los tests; en partida no se exige reproducibilidad.
 */
public interface Randomizer {

    boolean chance(double probability);
}
