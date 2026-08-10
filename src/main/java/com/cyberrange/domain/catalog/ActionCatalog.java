package com.cyberrange.domain.catalog;

import com.cyberrange.domain.model.Role;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Catalogo cargado en memoria. Es inmutable: si el instructor edita el
 * fichero hay que reiniciar, que es preferible a que una partida en curso
 * cambie de reglas a mitad de ronda.
 */
public final class ActionCatalog {

    private final Map<String, ActionCard> cardsById;

    public ActionCatalog(List<ActionCard> cards) {
        Map<String, ActionCard> byId = new LinkedHashMap<>();
        for (ActionCard card : cards) {
            byId.put(card.id(), card);
        }
        this.cardsById = Map.copyOf(byId);
    }

    public Optional<ActionCard> find(String id) {
        return Optional.ofNullable(cardsById.get(id));
    }

    public List<ActionCard> all() {
        return List.copyOf(cardsById.values());
    }

    public List<ActionCard> playableBy(Role side) {
        return cardsById.values().stream().filter(card -> card.isPlayableBy(side)).toList();
    }

    public List<ActionCard> twists() {
        return cardsById.values().stream().filter(card -> card.type() == CardType.TWIST).toList();
    }

    public int size() {
        return cardsById.size();
    }
}
