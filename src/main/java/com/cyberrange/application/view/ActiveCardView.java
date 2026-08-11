package com.cyberrange.application.view;

/**
 * Carta que sigue en pie. roundsRemaining vacio significa que aguanta hasta
 * el final de la mitad.
 */
public record ActiveCardView(String cardId, String side, Integer roundsRemaining) {
}
