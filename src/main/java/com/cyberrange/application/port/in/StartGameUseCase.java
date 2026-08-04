package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;

/**
 * Puerto de entrada: el instructor cierra el lobby y arranca la primera
 * ronda.
 */
public interface StartGameUseCase {

    void startGame(GameId gameId, ParticipantSession session);
}
