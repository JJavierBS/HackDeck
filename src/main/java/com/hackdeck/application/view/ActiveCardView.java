package com.hackdeck.application.view;

import java.util.Map;

/** roundsRemaining vacio significa que aguanta hasta el final de la mitad. */
public record ActiveCardView(String cardId, Map<String, String> cardName, String side, Integer roundsRemaining) {
}
