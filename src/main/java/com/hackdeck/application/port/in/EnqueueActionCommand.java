package com.hackdeck.application.port.in;

import java.util.Map;

/**
 * Lo que el cliente puede decidir al encolar: que carta juega y con que
 * parametros. Ni el bando ni el coste ni el ruido viajan aqui a proposito:
 * el bando lo deduce el servidor de la sesion y lo demas lo dice el
 * catalogo.
 */
public record EnqueueActionCommand(String cardId, Map<String, String> parameters) {

    public EnqueueActionCommand {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
