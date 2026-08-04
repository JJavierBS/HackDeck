package com.cyberrange.application.port.out;

import com.cyberrange.domain.model.ParticipantSession;

import java.util.Optional;

/**
 * Puerto de salida: emision y verificacion de las credenciales con las que
 * un participante prueba quien es en cada peticion. La tecnica concreta
 * (hoy un JWT firmado con clave simetrica) es un detalle del adaptador.
 */
public interface AccessTokenPort {

    String issue(ParticipantSession session);

    /**
     * Vacio si el token no es valido: firma incorrecta, caducado o
     * manipulado. Nunca lanza por un token invalido.
     */
    Optional<ParticipantSession> verify(String token);
}
