package com.cyberrange.adapter.catalog;

/**
 * El catalogo esta mal formado. Se lanza al arrancar para que el fallo se
 * vea en el arranque y no en mitad de una clase.
 */
public class InvalidCatalogException extends RuntimeException {

    public InvalidCatalogException(String message) {
        super(message);
    }

    public InvalidCatalogException(String message, Throwable cause) {
        super(message, cause);
    }
}
