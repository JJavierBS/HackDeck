package com.cyberrange.domain.model;

/**
 * Por que se gano el match. Se muestra al proyectar el resultado en clase,
 * asi que cada valor tiene que poder explicarse en una frase.
 */
public enum MatchOutcome {
    /** Solo un equipo derribo un pilar. */
    TAKEDOWN,
    /** Los dos derribaron y gana quien lo hizo en menos rondas. */
    TAKEDOWN_FASTER,
    /** Nadie derribo: gana quien defendio mejor su triada. */
    POINTS,
    /** Empate perfecto. */
    DRAW
}
