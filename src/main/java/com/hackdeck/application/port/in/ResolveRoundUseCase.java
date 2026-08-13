package com.hackdeck.application.port.in;

import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.ParticipantSession;
import com.hackdeck.domain.model.Game;

public interface ResolveRoundUseCase {

    Game resolveCurrentRound(GameId gameId, ParticipantSession session);
}
