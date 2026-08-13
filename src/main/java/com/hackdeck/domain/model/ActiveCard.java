package com.hackdeck.domain.model;

import java.util.Objects;

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
