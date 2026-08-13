package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;

public interface LaunchTwistUseCase {

    void launchTwist(GameId gameId, ParticipantSession session, String cardId);
}
