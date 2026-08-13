package com.cyberdeck.application.port.in;

import com.cyberdeck.domain.model.GameId;
import com.cyberdeck.domain.model.ParticipantSession;
import com.cyberdeck.domain.model.Game;

public interface ResolveRoundUseCase {

    Game resolveCurrentRound(GameId gameId, ParticipantSession session);
}
