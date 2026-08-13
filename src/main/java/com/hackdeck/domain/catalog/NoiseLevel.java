package com.hackdeck.domain.catalog;

public enum NoiseLevel {
    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int level;

    NoiseLevel(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }
}
