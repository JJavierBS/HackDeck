package com.cyberdeck.domain.model;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** El coste, el ruido y el efecto los pone el catalogo, no el cliente. */
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
