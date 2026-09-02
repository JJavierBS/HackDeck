package com.hackdeck.adapter.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.hackdeck.adapter.security.SecurityProperties;

import java.util.List;

@Component
public class StartupBanner {

    private static final Logger log = LoggerFactory.getLogger(StartupBanner.class);

    private final int port;
    private final String publicUrl;
    private final SecurityProperties security;

    public StartupBanner(
            @Value("${server.port:8080}") int port,
            @Value("${hackdeck.public-url:}") String publicUrl,
            SecurityProperties security
    ) {
        this.port = port;
        this.publicUrl = publicUrl != null ? publicUrl.trim() : "";
        this.security = security;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void mostrarDirecciones() {
        log.info("HackDeck listo. Instructor: http://localhost:{}", port);
        avisarSiLaCreacionEstaAbierta();
        if (!publicUrl.isEmpty()) {
            log.info("Los equipos entran por: {}", publicUrl);
            return;
        }
        List<String> locales = ConnectionInfoController.localAddresses();
        if (locales.isEmpty()) {
            return;
        }
        locales.forEach(host -> log.info("Los equipos entran por: http://{}:{}", host, port));
        log.info("Si no llegan, abre el puerto {} en el cortafuegos del equipo", port);
    }

    /**
     * Quedarse sin clave por descuido y quedarse sin clave a proposito se ven
     * igual desde fuera, asi que conviene decirlo en voz alta al arrancar.
     */
    private void avisarSiLaCreacionEstaAbierta() {
        if (security.hasInstructorKey()) {
            return;
        }
        log.warn("Crear partidas esta ABIERTO a cualquiera: no hay clave de instructor configurada");
        log.warn("Lo unico que frena los abusos son los cupos de hackdeck.limits; pon "
                + "HACKDECK_INSTRUCTOR_KEY si quieres cerrarlo");
    }
}
