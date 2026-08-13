package com.hackdeck.application.port.in;

import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.ParticipantSession;

/** Cierra la ronda solo si el instructor tiene puesto el modo automatico. */
public interface MarkReadyUseCase {

    void markReady(GameId gameId, ParticipantSession session);
}
