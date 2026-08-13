package com.hackdeck.application.port.in;

import com.hackdeck.domain.model.Game;
import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.ParticipantSession;

public interface GetGameStateUseCase {

    Game getGameState(GameId gameId, ParticipantSession session);
}
