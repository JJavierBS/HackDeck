package com.cyberdeck.application.port.out;

import com.cyberdeck.domain.model.ParticipantSession;
import com.cyberdeck.domain.model.TournamentSession;

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
