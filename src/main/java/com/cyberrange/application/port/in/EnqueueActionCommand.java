package com.cyberrange.application.port.in;

import java.util.Map;

/**
 * Lo que el cliente puede decidir al encolar una accion. El bando no viaja
 * aqui a proposito: lo deduce el servidor de la sesion, para que nadie
 * pueda encolar acciones en nombre del rival.
 */
public record EnqueueActionCommand(String actionType, Map<String, String> parameters, boolean noisy) {

    public EnqueueActionCommand {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
