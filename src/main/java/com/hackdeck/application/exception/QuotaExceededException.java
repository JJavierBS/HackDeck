package com.hackdeck.application.exception;

/**
 * Quien pide se ha pasado de su cupo. No es que el servidor este lleno (eso
 * es CapacityExceededException): es que este cliente en concreto ya tiene
 * demasiado abierto, y se arregla cerrando lo suyo.
 */
public class QuotaExceededException extends RuntimeException {

    public QuotaExceededException(String message) {
        super(message);
    }
}
