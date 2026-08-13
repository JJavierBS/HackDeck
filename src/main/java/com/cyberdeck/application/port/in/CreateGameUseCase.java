package com.cyberdeck.application.port.in;

import com.cyberdeck.domain.model.GameSettings;

public interface CreateGameUseCase {

    GameAccess createGame(GameSettings settings);
}
