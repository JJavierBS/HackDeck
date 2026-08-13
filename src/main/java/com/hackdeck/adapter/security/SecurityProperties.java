package com.hackdeck.adapter.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

/**
 * Con jwtSecret vacia se genera una clave aleatoria al arrancar y los tokens
 * mueren al reiniciar, igual que las partidas.
 *
 * Ojo con allowLoopbackWithoutKey detras de un proxy inverso: el proxy
 * aparece como cliente loopback y exime de la clave a todo el mundo.
 */
@ConfigurationProperties(prefix = "hackdeck.security")
public record SecurityProperties(
        @DefaultValue("") String jwtSecret,
        @DefaultValue("8h") Duration tokenTtl,
        @DefaultValue("") String instructorKey,
        @DefaultValue("true") boolean allowLoopbackWithoutKey,
        @DefaultValue("http://localhost:5173") List<String> corsAllowedOrigins) {

    public static final int MIN_SECRET_LENGTH = 32;

    public boolean hasJwtSecret() {
        return jwtSecret != null && !jwtSecret.isBlank();
    }

    public boolean hasInstructorKey() {
        return instructorKey != null && !instructorKey.isBlank();
    }
}
