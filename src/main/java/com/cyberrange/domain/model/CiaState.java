package com.cyberrange.domain.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Nivel de salud de cada pilar de la triada CIA (0 = derribado, 100 = intacto).
 * El calculo de impacto de una accion es una regla de juego y vive en el
 * motor de reglas, no aqui: este tipo solo modela el estado resultante.
 */
public final class CiaState {

    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 100;

    private final Map<CiaPillar, Integer> levels;

    public CiaState(Map<CiaPillar, Integer> levels) {
        this.levels = new EnumMap<>(Objects.requireNonNull(levels, "levels"));
    }

    public static CiaState intact() {
        Map<CiaPillar, Integer> levels = new EnumMap<>(CiaPillar.class);
        for (CiaPillar pillar : CiaPillar.values()) {
            levels.put(pillar, MAX_LEVEL);
        }
        return new CiaState(levels);
    }

    public int levelOf(CiaPillar pillar) {
        return levels.getOrDefault(pillar, MIN_LEVEL);
    }

    public boolean isPillarDown(CiaPillar pillar) {
        return levelOf(pillar) <= MIN_LEVEL;
    }

    public CiaState withImpact(CiaPillar pillar, int delta) {
        Map<CiaPillar, Integer> updated = new EnumMap<>(levels);
        updated.put(pillar, Math.clamp(levelOf(pillar) + delta, MIN_LEVEL, MAX_LEVEL));
        return new CiaState(updated);
    }

    public Map<CiaPillar, Integer> levels() {
        return Collections.unmodifiableMap(levels);
    }
}
