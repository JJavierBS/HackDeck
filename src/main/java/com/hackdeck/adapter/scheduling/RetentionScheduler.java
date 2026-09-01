package com.hackdeck.adapter.scheduling;

import com.hackdeck.application.port.in.PurgeStaleGamesUseCase;
import com.hackdeck.application.port.in.PurgeStaleTournamentsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * El barrendero. Sin el, un servidor publico acumula partidas hasta quedarse
 * sin memoria: nadie avisa de que una mesa se ha abandonado.
 */
@Component
public class RetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetentionScheduler.class);

    private final PurgeStaleGamesUseCase games;
    private final PurgeStaleTournamentsUseCase tournaments;

    public RetentionScheduler(PurgeStaleGamesUseCase games, PurgeStaleTournamentsUseCase tournaments) {
        this.games = games;
        this.tournaments = tournaments;
    }

    @Scheduled(fixedDelayString = "${hackdeck.limits.purge-interval:1m}")
    public void purgeStale() {
        try {
            int partidas = games.purgeStaleGames();
            int torneos = tournaments.purgeStaleTournaments();
            if (partidas > 0 || torneos > 0) {
                log.info("Se han purgado {} partida(s) y {} torneo(s) caducados", partidas, torneos);
            }
        } catch (RuntimeException e) {
            // Igual que el reloj de rondas: un fallo aqui no puede dejar el
            // proceso sin barrendero para siempre.
            log.error("Fallo al purgar partidas caducadas", e);
        }
    }
}
