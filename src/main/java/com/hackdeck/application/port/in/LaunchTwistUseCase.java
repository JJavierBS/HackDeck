package com.hackdeck.application.port.in;

import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.ParticipantSession;

public interface LaunchTwistUseCase {

    void launchTwist(GameId gameId, ParticipantSession session, String cardId);
}
