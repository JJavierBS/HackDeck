package com.hackdeck.application.port.in;

/** El reloj es del servidor para que todas las mesas del aula vayan igual. */
public interface ResolveExpiredRoundsUseCase {

    int resolveExpiredRounds();
}
