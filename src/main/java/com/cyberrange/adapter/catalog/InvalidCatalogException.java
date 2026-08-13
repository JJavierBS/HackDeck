package com.cyberrange.adapter.catalog;

public class InvalidCatalogException extends RuntimeException {

    public InvalidCatalogException(String message) {
        super(message);
    }

    public InvalidCatalogException(String message, Throwable cause) {
        super(message, cause);
    }
}
