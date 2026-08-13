package com.cyberdeck.adapter.security;

import com.cyberdeck.application.exception.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Controla quien puede crear partidas. Desde el propio equipo (el portatil
 * del instructor en el aula) no se pide nada; desde fuera hace falta la
 * clave configurada, para que un despliegue publico no se llene de partidas.
 */
@Component
public final class InstructorAccessGuard {

    public static final String INSTRUCTOR_KEY_HEADER = "X-Instructor-Key";

    private final SecurityProperties properties;

    public InstructorAccessGuard(SecurityProperties properties) {
        this.properties = properties;
    }

    public void requireInstructorAccess(HttpServletRequest request) {
        if (properties.allowLoopbackWithoutKey() && isLoopback(request.getRemoteAddr())) {
            return;
        }
        if (!properties.hasInstructorKey()) {
            throw new AccessDeniedException(
                    "Crear partidas desde fuera del servidor exige configurar cyberdeck.security.instructor-key");
        }
        if (!matchesInstructorKey(request.getHeader(INSTRUCTOR_KEY_HEADER))) {
            throw new AccessDeniedException("Clave de instructor incorrecta");
        }
    }

    private boolean matchesInstructorKey(String provided) {
        if (provided == null) {
            return false;
        }
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                properties.instructorKey().getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isLoopback(String remoteAddress) {
        if (remoteAddress == null) {
            return false;
        }
        try {
            return InetAddress.getByName(remoteAddress).isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
