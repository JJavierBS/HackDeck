package com.cyberrange.application.port.out;

import com.cyberrange.domain.model.ParticipantSession;
import com.cyberrange.domain.model.TournamentSession;

import java.util.Optional;

public interface AccessTokenPort {

    String issue(ParticipantSession session);

    String issueTournament(TournamentSession session);

    Optional<TournamentSession> verifyTournament(String token);

    /**
     * Vacio si el token no es valido: firma incorrecta, caducado o
     * manipulado. Nunca lanza por un token invalido.
     */
    Optional<ParticipantSession> verify(String token);
}
