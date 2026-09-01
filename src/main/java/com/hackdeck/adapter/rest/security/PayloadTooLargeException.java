package com.hackdeck.adapter.rest.security;

/**
 * El cuerpo de la peticion se pasa del tope. Es RuntimeException a proposito:
 * salta desde la lectura del cuerpo, a mitad del parseo, y asi sube entera
 * hasta el manejador de errores en vez de confundirse con un JSON malformado.
 */
public class PayloadTooLargeException extends RuntimeException {

    public PayloadTooLargeException(long maxBytes) {
        super("El cuerpo de la peticion supera los " + maxBytes + " bytes permitidos");
    }
}
