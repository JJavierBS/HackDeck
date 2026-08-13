package com.cyberdeck.domain.model;

import java.util.Objects;
import java.util.UUID;

public record TournamentId(UUID value) {

    public TournamentId {
        Objects.requireNonNull(value, "value");
    }

    public static TournamentId newId() {
        return new TournamentId(UUID.randomUUID());
    }

    public static TournamentId of(String rawValue) {
        return new TournamentId(UUID.fromString(rawValue));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
