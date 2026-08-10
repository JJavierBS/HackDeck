package com.cyberrange.domain.model;

/**
 * Lectura cualitativa de un pilar. El atacante no ve el numero exacto de la
 * triada que esta atacando, solo como de tocada esta: sabe si va bien
 * encaminado sin poder calcular al punto cuanto le falta.
 */
// TODO: confirmar si el atacante debe ver el valor exacto en vez de esto.
public enum PillarStatus {
    INTACT,
    DAMAGED,
    CRITICAL,
    DOWN;

    private static final int DAMAGED_FROM = 80;
    private static final int CRITICAL_FROM = 40;

    public static PillarStatus of(int level) {
        if (level <= CiaState.MIN_LEVEL) {
            return DOWN;
        }
        if (level < CRITICAL_FROM) {
            return CRITICAL;
        }
        if (level < DAMAGED_FROM) {
            return DAMAGED;
        }
        return INTACT;
    }
}
