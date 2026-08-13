package com.hackdeck.domain.exception;

public class GameNotJoinableException extends RuntimeException {

    public GameNotJoinableException(String reason) {
        super(reason);
    }
}
