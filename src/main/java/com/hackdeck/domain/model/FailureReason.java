package com.hackdeck.domain.model;

/**
 * Por que fallo un ataque. No hay motivo de kill chain: sin la fase previa la
 * carta no se puede ni encolar, asi que nunca llega a fallar por eso.
 */
public enum FailureReason {
    COUNTERED,
    BAD_LUCK
}
