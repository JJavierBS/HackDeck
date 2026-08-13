package com.cyberdeck.domain.exception;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(String reference) {
        super("No existe ninguna partida: " + reference);
    }
}
