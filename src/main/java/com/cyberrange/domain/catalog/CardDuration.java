package com.cyberrange.domain.catalog;

public enum CardDuration {
    /** Se aplica y se acaba en la ronda en la que se juega. */
    INSTANT,
    /** Dura un numero concreto de rondas y caduca. */
    ROUNDS,
    /** Se queda hasta el final de la mitad. */
    PERMANENT
}
