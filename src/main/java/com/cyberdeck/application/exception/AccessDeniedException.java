package com.cyberdeck.application.exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String reason) {
        super(reason);
    }
}
