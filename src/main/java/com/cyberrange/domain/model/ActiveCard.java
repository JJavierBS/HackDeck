package com.cyberrange.domain.model;

import java.util.Objects;

/**
 * Una carta que sigue haciendo efecto despues de la ronda en la que se
 * jugo: una capa defensiva, una persistencia del atacante o un twist en
 * curso.
 *
 * @param roundsRemaining rondas que le quedan, o PERMANENT si aguanta hasta
 *                        el final de la mitad.
 */
public record ActiveCard(String cardId, Role side, int roundsRemaining) {

    public static final int PERMANENT = -1;

    public ActiveCard {
        Objects.requireNonNull(cardId, "cardId");
    }

    public static ActiveCard permanent(String cardId, Role side) {
        return new ActiveCard(cardId, side, PERMANENT);
    }

    public boolean isPermanent() {
        return roundsRemaining == PERMANENT;
    }

    public ActiveCard afterRound() {
        return isPermanent() ? this : new ActiveCard(cardId, side, roundsRemaining - 1);
    }

    public boolean isExpired() {
        return !isPermanent() && roundsRemaining <= 0;
    }
}
