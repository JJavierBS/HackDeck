package com.hackdeck.domain.exception;

public class AlreadyDeployedException extends RuntimeException {

    public AlreadyDeployedException(String message) {
        super(message);
    }
}
