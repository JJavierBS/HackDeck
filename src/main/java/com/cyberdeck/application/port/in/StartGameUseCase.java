package com.cyberdeck.application.port.in;

import com.cyberdeck.domain.model.GameId;
import com.cyberdeck.domain.model.ParticipantSession;

public interface StartGameUseCase {

    void startGame(GameId gameId, ParticipantSession session);
}
