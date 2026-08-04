package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameSettings;

/**
 * Puerto de entrada: el instructor crea una nueva partida en fase de
 * preparacion y recibe el codigo con el que se uniran los equipos.
 */
public interface CreateGameUseCase {

    GameAccess createGame(GameSettings settings);
}
