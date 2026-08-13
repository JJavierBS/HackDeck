package com.cyberdeck.application.port.in;

import com.cyberdeck.domain.model.GameId;
import com.cyberdeck.domain.model.ParticipantSession;

public interface LaunchTwistUseCase {

    void launchTwist(GameId gameId, ParticipantSession session, String cardId);
}
