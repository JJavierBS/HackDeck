package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;

public interface StartGameUseCase {

    void startGame(GameId gameId, ParticipantSession session);
}
