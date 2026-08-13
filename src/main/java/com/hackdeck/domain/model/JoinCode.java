package com.hackdeck.domain.model;

import java.security.SecureRandom;
import java.util.Objects;

/**
 * Codigo corto con el que los equipos se unen a una partida, estilo Kahoot.
 * El alfabeto excluye los caracteres que se confunden al dictarlos o al
 * leerlos proyectados: I, O, 0, 1.
 */
public record JoinCode(String value) {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public JoinCode {
        Objects.requireNonNull(value, "value");
        if (value.length() != LENGTH) {
            throw new IllegalArgumentException("El codigo de partida tiene " + LENGTH + " caracteres");
        }
        for (char character : value.toCharArray()) {
            if (ALPHABET.indexOf(character) < 0) {
                throw new IllegalArgumentException("Codigo de partida invalido: " + value);
            }
        }
    }

    public static JoinCode generate() {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new JoinCode(code.toString());
    }

    public static JoinCode of(String rawValue) {
        Objects.requireNonNull(rawValue, "rawValue");
        return new JoinCode(rawValue.strip().toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
