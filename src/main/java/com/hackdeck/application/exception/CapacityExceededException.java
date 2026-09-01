package com.hackdeck.application.exception;

/**
 * El servidor esta lleno. Es distinto de un error del cliente: la peticion
 * era correcta y volvera a valer cuando se libere sitio, por eso sale como
 * 503 y no como 4xx.
 */
public class CapacityExceededException extends RuntimeException {

    public CapacityExceededException(String message) {
        super(message);
    }
}
