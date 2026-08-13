package com.hackdeck.application.port.in;

import com.hackdeck.domain.model.JoinCode;

public interface JoinGameUseCase {

    GameAccess joinGame(JoinCode joinCode, String displayName);
}
