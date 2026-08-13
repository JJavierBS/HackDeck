package com.hackdeck.domain.exception;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(String reference) {
        super("No existe ninguna partida: " + reference);
    }
}
