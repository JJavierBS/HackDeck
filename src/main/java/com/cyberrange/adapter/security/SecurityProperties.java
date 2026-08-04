package com.cyberrange.adapter.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

/**
 * Configuracion de seguridad, externalizable por variables de entorno para
 * poder desplegar el mismo jar en el aula y en la nube.
 *
 * @param jwtSecret               clave HMAC de al menos 32 caracteres. Si se
 *                                deja vacia se genera una aleatoria al
 *                                arrancar, con lo que los tokens mueren al
 *                                reiniciar (igual que las partidas, que son
 *                                in-memory).
 * @param tokenTtl                validez del token; debe cubrir una sesion de
 *                                clase entera.
 * @param instructorKey           clave que hay que enviar para crear partidas.
 * @param allowLoopbackWithoutKey exime de la clave a las peticiones locales.
 *                                Ponlo a false si el backend esta detras de un
 *                                proxy inverso: el proxy aparece como cliente
 *                                loopback y cualquiera quedaria exento.
 * @param corsAllowedOrigins      origenes del frontend en desarrollo. En
 *                                produccion el jar sirve el front, mismo
 *                                origen y CORS deja de intervenir.
 */
@ConfigurationProperties(prefix = "cyberrange.security")
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
