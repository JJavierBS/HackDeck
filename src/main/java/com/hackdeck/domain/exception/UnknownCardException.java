package com.hackdeck.domain.exception;

public class UnknownCardException extends RuntimeException {

    public UnknownCardException(String message) {
        super(message);
    }
}
