package com.cyberrange.adapter.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StartupBanner {

    private static final Logger log = LoggerFactory.getLogger(StartupBanner.class);

    private final int port;

    public StartupBanner(@Value("${server.port:8080}") int port) {
        this.port = port;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void mostrarDirecciones() {
        log.info("Cyber Range listo. Instructor: http://localhost:{}", port);
        List<String> locales = ConnectionInfoController.localAddresses();
        if (locales.isEmpty()) {
            return;
        }
        locales.forEach(host -> log.info("Los equipos entran por: http://{}:{}", host, port));
        log.info("Si no llegan, abre el puerto {} en el cortafuegos del equipo", port);
    }
}
