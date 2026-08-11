package com.cyberrange.domain.model;

/**
 * Que clase de cosa ocurrio. El tipo va aparte del texto para que el
 * historial se pueda filtrar y reproducir sin tener que leer frases.
 */
public enum GameEventType {
    TEAM_JOINED,
    MATCH_STARTED,
    HALF_STARTED,
    ATTACK,
    DEFENCE,
    TWIST_LAUNCHED,
    /** Cierre de ronda; es el unico que lleva la foto de la triada. */
    ROUND_RESOLVED,
    MATCH_FINISHED
}
