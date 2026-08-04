package com.cyberrange.adapter.rest.security;

/**
 * Falta el token o no es valido: el peticionario no esta identificado.
 */
public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(String reason) {
        super(reason);
    }
}
