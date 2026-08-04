package com.cyberrange.domain.model;

/**
 * Equipo dentro de un match. No confundir con {@link Role}: el equipo es
 * estable durante todo el match, mientras que el bando (atacante/defensor)
 * cambia al pasar a la segunda mitad.
 */
public enum TeamId {
    A,
    B
}
