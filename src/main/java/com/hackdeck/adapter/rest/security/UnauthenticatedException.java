package com.hackdeck.adapter.rest.security;

public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(String reason) {
        super(reason);
    }
}
