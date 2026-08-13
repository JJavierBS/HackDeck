package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameSettings;

public interface CreateGameUseCase {

    GameAccess createGame(GameSettings settings);
}
