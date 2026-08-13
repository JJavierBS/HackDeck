package com.hackdeck.adapter.scheduling;

import com.hackdeck.application.port.in.ResolveExpiredRoundsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Solo cierra rondas de partidas con el modo automatico puesto; en manual el
 * tiempo se agota y no pasa nada hasta que el instructor decide.
 */
@Component
public class RoundTimerScheduler {

    private static final Logger log = LoggerFactory.getLogger(RoundTimerScheduler.class);

    private final ResolveExpiredRoundsUseCase resolveExpiredRounds;

    public RoundTimerScheduler(ResolveExpiredRoundsUseCase resolveExpiredRounds) {
        this.resolveExpiredRounds = resolveExpiredRounds;
    }

    @Scheduled(fixedDelayString = "${hackdeck.round-timer.check-millis:1000}")
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
