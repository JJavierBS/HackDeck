package com.cyberrange.application.port.out;

import com.cyberrange.domain.model.ParticipantSession;

import java.util.Optional;

public interface AccessTokenPort {

    String issue(ParticipantSession session);

    /**
     * Vacio si el token no es valido: firma incorrecta, caducado o
     * manipulado. Nunca lanza por un token invalido.
     */
    Optional<ParticipantSession> verify(String token);
}
