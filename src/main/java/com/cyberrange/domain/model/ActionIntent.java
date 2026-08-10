package com.cyberrange.domain.model;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Intencion de accion encolada por un participante para la ronda actual.
 * Solo dice que carta juega y con que parametros: el coste, el ruido y el
 * efecto los pone el catalogo, no el cliente.
 */
public record ActionIntent(
        UUID id,
        Role team,
        String cardId,
        Map<String, String> parameters) {

    public ActionIntent {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(team, "team");
        Objects.requireNonNull(cardId, "cardId");
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
