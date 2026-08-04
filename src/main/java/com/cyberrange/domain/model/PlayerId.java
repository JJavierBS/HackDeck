package com.cyberrange.domain.model;

import java.util.Objects;
import java.util.UUID;

public record PlayerId(UUID value) {

    public PlayerId {
        Objects.requireNonNull(value, "value");
    }

    public static PlayerId newId() {
        return new PlayerId(UUID.randomUUID());
    }

    public static PlayerId of(String rawValue) {
        return new PlayerId(UUID.fromString(rawValue));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
