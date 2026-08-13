package com.cyberdeck.application.port.in;

import com.cyberdeck.domain.model.GameId;
import com.cyberdeck.domain.model.ParticipantSession;

public interface EnqueueActionUseCase {

    void enqueueAction(GameId gameId, ParticipantSession session, EnqueueActionCommand command);
}
