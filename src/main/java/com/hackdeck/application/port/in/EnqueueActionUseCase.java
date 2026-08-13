package com.hackdeck.application.port.in;

import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.ParticipantSession;

public interface EnqueueActionUseCase {

    void enqueueAction(GameId gameId, ParticipantSession session, EnqueueActionCommand command);

    void dequeueAction(GameId gameId, ParticipantSession session, java.util.UUID intentId);

    void reorderQueue(GameId gameId, ParticipantSession session, java.util.List<java.util.UUID> intentIds);
}
