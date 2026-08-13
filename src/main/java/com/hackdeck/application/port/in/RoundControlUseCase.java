package com.hackdeck.application.port.in;

import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.ParticipantSession;

public interface RoundControlUseCase {

    /** Con el modo automatico la ronda se cierra sola; sin el, solo avisa. */
    void setAutoResolve(GameId gameId, ParticipantSession session, boolean enabled);

    void closeHalf(GameId gameId, ParticipantSession session);

    void closeMatch(GameId gameId, ParticipantSession session);
}
