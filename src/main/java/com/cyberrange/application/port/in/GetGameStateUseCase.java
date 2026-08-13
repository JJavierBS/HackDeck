package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;

public interface GetGameStateUseCase {

    Game getGameState(GameId gameId, ParticipantSession session);
}
