package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;
import com.cyberrange.domain.model.Game;

/**
 * Puerto de entrada: resuelve la ronda en curso (todas las acciones
 * encoladas se aplican), difunde el nuevo estado y devuelve la partida
 * ya actualizada.
 */
public interface ResolveRoundUseCase {

    Game resolveCurrentRound(GameId gameId, ParticipantSession session);
}
