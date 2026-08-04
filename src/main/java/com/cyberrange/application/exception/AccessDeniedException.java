package com.cyberrange.application.exception;

/**
 * El peticionario esta identificado pero no puede hacer lo que pide: no
 * pertenece a la partida, no es el instructor o no juega ese bando.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String reason) {
        super(reason);
    }
}
