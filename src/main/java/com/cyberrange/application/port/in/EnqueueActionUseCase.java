package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;

public interface EnqueueActionUseCase {

    void enqueueAction(GameId gameId, ParticipantSession session, EnqueueActionCommand command);
}
