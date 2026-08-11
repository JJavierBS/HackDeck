package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;

/**
 * Puerto de entrada: un equipo declara que ya ha decidido su jugada. Cierra
 * la ronda solo si el instructor tiene puesto el modo automatico.
 */
public interface MarkReadyUseCase {

    void markReady(GameId gameId, ParticipantSession session);
}
