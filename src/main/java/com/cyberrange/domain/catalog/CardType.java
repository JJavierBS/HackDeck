package com.cyberrange.domain.catalog;

public enum CardType {
    /** Carta normal, se compra con el presupuesto de la ronda. */
    ACTION,
    /** Un solo uso, cara: se paga con lo ahorrado. */
    POWERUP,
    /** Carta de escenario que lanza el instructor; no se compra. */
    TWIST
}
