package com.cyberdeck.application.port.in;

import com.cyberdeck.domain.model.Game;
import com.cyberdeck.domain.model.GameId;
import com.cyberdeck.domain.model.ParticipantSession;

public interface GetGameStateUseCase {

    Game getGameState(GameId gameId, ParticipantSession session);
}
