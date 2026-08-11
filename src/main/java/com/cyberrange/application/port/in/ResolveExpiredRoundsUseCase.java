package com.cyberrange.application.port.in;

/**
 * Puerto de entrada del propio servidor: cierra las rondas cuyo tiempo se
 * ha agotado en las partidas que tengan el modo automatico. El reloj es del
 * servidor, no del navegador, para que todas las mesas vayan igual.
 */
public interface ResolveExpiredRoundsUseCase {

    /**
     * @return cuantas rondas se han cerrado.
     */
    int resolveExpiredRounds();
}
