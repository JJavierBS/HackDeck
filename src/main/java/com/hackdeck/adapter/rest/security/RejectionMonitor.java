package com.hackdeck.adapter.rest.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Cuenta los rechazos y los resume cada minuto en una sola linea.
 *
 * No se registra uno por uno a proposito: bajo un ataque, escribir una linea
 * por peticion rechazada convierte el log en el siguiente recurso a agotar.
 *
 * Tampoco se anotan las direcciones, solo cuantas distintas hay: para
 * bloquear a alguien esta el log del proxy, que es quien tiene esa
 * responsabilidad. Aqui lo util es saber si esto esta pasando y a que escala.
 */
@Component
public final class RejectionMonitor {

    /** Con mas direcciones distintas que esto, el numero exacto ya no cambia la decision. */
    private static final int MAX_DIRECCIONES_SEGUIDAS = 1_000;

    private static final Logger log = LoggerFactory.getLogger(RejectionMonitor.class);

    private final Map<String, AtomicLong> vecesPorMotivo = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> direccionesPorMotivo = new ConcurrentHashMap<>();

    public void record(String motivo, String direccion) {
        vecesPorMotivo.computeIfAbsent(motivo, ignorado -> new AtomicLong()).incrementAndGet();
        Set<String> direcciones =
                direccionesPorMotivo.computeIfAbsent(motivo, ignorado -> ConcurrentHashMap.newKeySet());
        if (direccion != null && direcciones.size() < MAX_DIRECCIONES_SEGUIDAS) {
            direcciones.add(direccion);
        }
    }

    @Scheduled(fixedDelayString = "${hackdeck.limits.rejection-summary-millis:60000}")
    public void logSummary() {
        drainSummary().ifPresent(resumen -> log.warn("Peticiones rechazadas: {}", resumen));
    }

    /**
     * Vacia lo acumulado y lo devuelve ya redactado, o nada si no hubo
     * rechazos: en un servidor tranquilo no debe aparecer una linea por
     * minuto diciendo que no pasa nada.
     */
    public Optional<String> drainSummary() {
        if (vecesPorMotivo.isEmpty()) {
            return Optional.empty();
        }
        Map<String, AtomicLong> veces = Map.copyOf(vecesPorMotivo);
        Map<String, Set<String>> direcciones = Map.copyOf(direccionesPorMotivo);
        vecesPorMotivo.clear();
        direccionesPorMotivo.clear();

        return Optional.of(veces.entrySet().stream()
                .sorted((una, otra) -> Long.compare(otra.getValue().get(), una.getValue().get()))
                .map(entrada -> entrada.getKey() + "=" + entrada.getValue().get()
                        + " (" + direcciones.getOrDefault(entrada.getKey(), Set.of()).size() + " direcciones)")
                .collect(Collectors.joining(", ")));
    }
}
