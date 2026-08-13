package com.hackdeck.application.port.in;

import com.hackdeck.domain.model.GameSettings;

public interface CreateGameUseCase {

    GameAccess createGame(GameSettings settings);
}
