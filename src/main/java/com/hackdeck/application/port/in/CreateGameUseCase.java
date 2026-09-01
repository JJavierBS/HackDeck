package com.hackdeck.application.port.in;

import com.hackdeck.domain.model.GameSettings;

public interface CreateGameUseCase {

    /**
     * @param creator identifica a quien crea, para el cupo por creador. Es
     *                opaco aqui: quien decide que es (hoy, la direccion de la
     *                peticion) es el adaptador.
     */
    GameAccess createGame(GameSettings settings, String creator);
}
