package com.cyberrange.domain.catalog;

/**
 * Cuanto se nota una accion. Se compara con el nivel de deteccion del
 * defensor para decidir si la ve.
 */
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
