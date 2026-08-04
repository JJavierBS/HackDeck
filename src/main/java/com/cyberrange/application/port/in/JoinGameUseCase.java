package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.JoinCode;

/**
 * Puerto de entrada: un equipo se une a una partida con el codigo y un
 * nombre elegido por ellos, sin registro previo. El servidor le asigna
 * equipo y le devuelve su token.
 */
public interface JoinGameUseCase {

    GameAccess joinGame(JoinCode joinCode, String displayName);
}
