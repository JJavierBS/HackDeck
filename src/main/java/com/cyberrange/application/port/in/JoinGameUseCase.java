package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.JoinCode;

public interface JoinGameUseCase {

    GameAccess joinGame(JoinCode joinCode, String displayName);
}
