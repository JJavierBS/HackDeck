package com.hackdeck.application.port.in;

import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.ParticipantSession;

public interface StartGameUseCase {

    void startGame(GameId gameId, ParticipantSession session);
}
