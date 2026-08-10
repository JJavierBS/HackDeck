package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;

/**
 * Puerto de entrada: el instructor lanza una carta de escenario sobre la
 * mitad en curso.
 */
public interface LaunchTwistUseCase {

    void launchTwist(GameId gameId, ParticipantSession session, String cardId);
}
