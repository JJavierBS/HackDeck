package com.cyberdeck.application.port.in;

import com.cyberdeck.domain.model.JoinCode;

public interface JoinGameUseCase {

    GameAccess joinGame(JoinCode joinCode, String displayName);
}
