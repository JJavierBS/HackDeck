package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;

/**
 * Puerto de entrada: un atacante o defensor encola una accion para la
 * ronda en curso, dentro de su presupuesto disponible.
 */
public interface EnqueueActionUseCase {

    void enqueueAction(GameId gameId, ParticipantSession session, EnqueueActionCommand command);
}
