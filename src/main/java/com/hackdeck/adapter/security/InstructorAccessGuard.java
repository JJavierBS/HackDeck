package com.hackdeck.adapter.security;

import com.hackdeck.application.exception.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Controla quien puede crear partidas.
 *
 * La clave es opcional: si no se configura, crear queda abierto a cualquiera,
 * que es lo que se quiere en un despliegue publico. Lo que impide entonces
 * que el servidor se llene no es una clave, sino los cupos por direccion y
 * los topes de §4.1: cuantas partidas vivas puede tener cada uno y cuantas
 * caben en total.
 *
 * Con la clave configurada se vuelve al modo cerrado, y desde el propio
 * equipo (el portatil del instructor en el aula) sigue sin pedirse nada.
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
            return;
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
