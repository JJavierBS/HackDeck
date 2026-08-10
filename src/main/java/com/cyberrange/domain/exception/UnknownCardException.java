package com.cyberrange.domain.exception;

/**
 * Se ha intentado jugar una carta que no esta en el catalogo o que no es de
 * ese bando.
 */
public class UnknownCardException extends RuntimeException {

    public UnknownCardException(String message) {
        super(message);
    }
}
