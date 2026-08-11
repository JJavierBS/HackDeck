package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;

/**
 * Puerto de entrada: el mando del instructor sobre el ritmo de la partida.
 */
public interface RoundControlUseCase {

    /** Con el modo automatico la ronda se cierra sola; sin el, solo avisa. */
    void setAutoResolve(GameId gameId, ParticipantSession session, boolean enabled);

    void closeHalf(GameId gameId, ParticipantSession session);

    void closeMatch(GameId gameId, ParticipantSession session);
}
