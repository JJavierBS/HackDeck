package com.cyberrange.domain.model;

/**
 * Por que fallo una accion. Es lo que convierte un "no ha funcionado" en una
 * leccion: el alumno tiene que poder saber si fallo por saltarse la kill
 * chain, por una defensa del rival o por mala suerte.
 */
public enum FailureReason {
    /** Se lanzo sin haber desbloqueado la fase previa. */
    KILL_CHAIN,
    /** Una defensa del rival lo neutralizo. */
    COUNTERED,
    /** Salio mal, sin mas. */
    BAD_LUCK
}
