package com.hackdeck.domain.model;

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
