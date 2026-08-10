package com.cyberrange.domain.catalog;

/**
 * Fases por las que progresa el atacante. Atacar un pilar directamente esta
 * permitido, pero sin las fases previas la probabilidad de exito es mucho
 * mas baja: es lo que empuja a los alumnos a recorrer la cadena.
 */
public enum KillChainPhase {
    RECON,
    ACCESS,
    ESCALATION,
    IMPACT
}
