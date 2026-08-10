package com.cyberrange.domain.catalog;

/**
 * Efectos especiales que una carta puede tener ademas de su impacto. El
 * motor de reglas los interpreta; el catalogo solo declara cuales trae.
 */
public enum CardEffect {
    REVEALS_DEFENCES,
    REVEALS_ROUND,
    REVEALS_PREVIOUS_ROUND,
    SILENCES_ROUND,
    PERSISTENCE,
    REMOVES_PERSISTENCE,
    AMPLIFIES_IMPACT,
    DOUBLE_IMPACT,
    DOUBLE_REPAIR,
    IGNORES_COUNTERS,
    BLOCKS_CHOSEN_ATTACK,
    CHEAPER_RESPONSE,
    EXTRA_BUDGET,
    BUDGET_CUT,
    WEAKENS_PERIMETER
}
