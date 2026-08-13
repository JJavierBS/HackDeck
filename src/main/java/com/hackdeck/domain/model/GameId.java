package com.hackdeck.domain.model;

import java.util.Objects;
import java.util.UUID;

public record GameId(UUID value) {

    public GameId {
        Objects.requireNonNull(value, "value");
    }

    public static GameId newId() {
        return new GameId(UUID.randomUUID());
    }

    public static GameId of(String rawValue) {
        return new GameId(UUID.fromString(rawValue));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
