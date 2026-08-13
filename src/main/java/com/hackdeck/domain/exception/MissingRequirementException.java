package com.hackdeck.domain.exception;

public class MissingRequirementException extends RuntimeException {

    public MissingRequirementException(String message) {
        super(message);
    }
}
