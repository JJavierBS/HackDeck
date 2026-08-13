package com.cyberdeck.application.port.in;

import com.cyberdeck.domain.model.GameId;
import com.cyberdeck.domain.model.ParticipantSession;

/** Cierra la ronda solo si el instructor tiene puesto el modo automatico. */
public interface MarkReadyUseCase {

    void markReady(GameId gameId, ParticipantSession session);
}
