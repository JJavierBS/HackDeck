package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;
import com.cyberrange.domain.rules.RoundResolution;

/**
 * Puerto de entrada: resuelve la ronda en curso (todas las acciones
 * encoladas se aplican) y difunde el nuevo estado a los participantes.
 */
public interface ResolveRoundUseCase {

    RoundResolution resolveCurrentRound(GameId gameId, ParticipantSession session);
}
