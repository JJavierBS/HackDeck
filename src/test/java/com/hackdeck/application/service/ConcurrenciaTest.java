package com.hackdeck.application.service;

import com.hackdeck.apoyo.AzarControlado;
import com.hackdeck.apoyo.Cartas;
import com.hackdeck.apoyo.Dobles;
import com.hackdeck.apoyo.Partidas;
import com.hackdeck.application.port.in.GameAccess;
import com.hackdeck.domain.catalog.KillChainPhase;
import com.hackdeck.domain.model.Game;
import com.hackdeck.domain.model.GameSettings;
import com.hackdeck.domain.model.Participant;
import com.hackdeck.domain.model.ParticipantSession;
import com.hackdeck.domain.model.TeamId;
import com.hackdeck.domain.rules.DefaultRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CyclicBarrier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Los dos equipos de una mesa pulsan a la vez, que es justo lo que pasa
 * cuando el aula entera juega contrarreloj.
 */
class ConcurrenciaTest {

    private static final int RONDAS = 19;

    private Dobles.Repositorio repositorio;
    private GameApplicationService servicio;

    @BeforeEach
    void preparar() {
        repositorio = new Dobles.Repositorio();
        servicio = new GameApplicationService(
                repositorio,
                new Dobles.Difusor(),
                new DefaultRuleEngine(
                        Partidas.catalogo(
                                Cartas.ataque("ataque").fase(KillChainPhase.RECON).cuesta(10).build(),
                                Cartas.defensa("defensa").cuesta(10).build()),
                        AzarControlado.siempre()),
                new Dobles.Tokens());
    }

    @Test
    void dos_equipos_confirmando_a_la_vez_cierran_la_ronda_una_sola_vez() throws Exception {
        GameSettings ajustes = new GameSettings(RONDAS + 1, Duration.ofMinutes(10), 100, 20);
        GameAccess acceso = servicio.createGame(ajustes);
        servicio.joinGame(acceso.joinCode(), "Rojos");
        servicio.joinGame(acceso.joinCode(), "Azules");
        Game partida = repositorio.findById(acceso.gameId()).orElseThrow();
        servicio.startGame(partida.id(), sesionDe(partida, null));
        servicio.setAutoResolve(partida.id(), sesionDe(partida, null), true);

        for (int ronda = 1; ronda <= RONDAS; ronda++) {
            confirmarALaVez(partida);
            assertThat(partida.currentHalf().currentRound().number()).isEqualTo(ronda + 1);
        }
    }

    private void confirmarALaVez(Game partida) throws Exception {
        CyclicBarrier salida = new CyclicBarrier(2);
        Thread rojos = new Thread(() -> confirmar(salida, partida, TeamId.A));
        Thread azules = new Thread(() -> confirmar(salida, partida, TeamId.B));
        rojos.start();
        azules.start();
        rojos.join();
        azules.join();
    }

    private void confirmar(CyclicBarrier salida, Game partida, TeamId equipo) {
        try {
            salida.await();
            servicio.markReady(partida.id(), sesionDe(partida, equipo));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static ParticipantSession sesionDe(Game partida, TeamId equipo) {
        Participant participante = equipo == null
                ? partida.instructor()
                : partida.playerOf(equipo).orElseThrow();
        return new ParticipantSession(partida.id(), participante);
    }
}
