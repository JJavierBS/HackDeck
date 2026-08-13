package com.cyberrange.domain.exception;

public class MissingRequirementException extends RuntimeException {

    public MissingRequirementException(String message) {
        super(message);
    }
}
