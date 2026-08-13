package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;

/** Cierra la ronda solo si el instructor tiene puesto el modo automatico. */
public interface MarkReadyUseCase {

    void markReady(GameId gameId, ParticipantSession session);
}
