package com.cyberrange.adapter.scheduling;

import com.cyberrange.application.port.in.ResolveExpiredRoundsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * El reloj de la ronda es del servidor, no del navegador: en el aula hay
 * varias mesas y los relojes de sus dispositivos no coinciden.
 *
 * Solo cierra rondas de partidas con el modo automatico puesto; con el
 * modo manual el tiempo se agota y no pasa nada hasta que el instructor
 * decide.
 */
@Component
public class RoundTimerScheduler {

    private static final Logger log = LoggerFactory.getLogger(RoundTimerScheduler.class);

    private final ResolveExpiredRoundsUseCase resolveExpiredRounds;

    public RoundTimerScheduler(ResolveExpiredRoundsUseCase resolveExpiredRounds) {
        this.resolveExpiredRounds = resolveExpiredRounds;
    }

    @Scheduled(fixedDelayString = "${cyberrange.round-timer.check-millis:1000}")
    public void closeExpiredRounds() {
        try {
            int resolved = resolveExpiredRounds.resolveExpiredRounds();
            if (resolved > 0) {
                log.info("El reloj ha cerrado {} ronda(s)", resolved);
            }
        } catch (RuntimeException e) {
            // Un fallo al cerrar una ronda no puede tumbar el reloj de todas
            // las demas mesas del aula.
            log.error("Fallo al cerrar rondas expiradas", e);
        }
    }
}
