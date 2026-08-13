package com.hackdeck.application.service;

import com.hackdeck.apoyo.AzarControlado;
import com.hackdeck.apoyo.Cartas;
import com.hackdeck.apoyo.Dobles;
import com.hackdeck.apoyo.Partidas;
import com.hackdeck.application.exception.AccessDeniedException;
import com.hackdeck.application.port.in.EnqueueActionCommand;
import com.hackdeck.application.port.in.GameAccess;
import com.hackdeck.domain.catalog.KillChainPhase;
import com.hackdeck.domain.exception.InsufficientBudgetException;
import com.hackdeck.domain.exception.UnknownCardException;
import com.hackdeck.domain.model.Game;
import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.JoinCode;
import com.hackdeck.domain.model.Participant;
import com.hackdeck.domain.model.ParticipantSession;
import com.hackdeck.domain.model.Role;
import com.hackdeck.domain.model.TeamId;
import com.hackdeck.domain.rules.DefaultRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El cliente puede mandar lo que quiera: quien decide es el servidor.
 */
class AutorizacionTest {

    private static final String ATAQUE = "ataque";
    private static final String DEFENSA = "defensa";

    private Dobles.Repositorio repositorio;
    private Dobles.Difusor difusor;
    private GameApplicationService servicio;

    @BeforeEach
    void preparar() {
        repositorio = new Dobles.Repositorio();
        difusor = new Dobles.Difusor();
        servicio = new GameApplicationService(
                repositorio,
                difusor,
                new DefaultRuleEngine(
                        Partidas.catalogo(
                                Cartas.ataque(ATAQUE).fase(KillChainPhase.RECON).cuesta(10).build(),
                                Cartas.defensa(DEFENSA).cuesta(10).build()),
                        AzarControlado.siempre()),
                new Dobles.Tokens());
    }

    private Game partidaEnCurso() {
        GameAccess acceso = servicio.createGame(Partidas.AJUSTES);
        servicio.joinGame(acceso.joinCode(), "Rojos");
        servicio.joinGame(acceso.joinCode(), "Azules");
        Game partida = repositorio.findById(acceso.gameId()).orElseThrow();
        servicio.startGame(partida.id(), sesionDe(partida, null));
        return partida;
    }

    private static ParticipantSession sesionDe(Game partida, TeamId equipo) {
        Participant participante = equipo == null
                ? partida.instructor()
                : partida.playerOf(equipo).orElseThrow();
        return new ParticipantSession(partida.id(), participante);
    }

    @Test
    void crear_partida_devuelve_codigo_y_token() {
        GameAccess acceso = servicio.createGame(Partidas.AJUSTES);

        assertThat(acceso.token()).isNotBlank();
        assertThat(acceso.joinCode().value()).hasSize(6);
        assertThat(acceso.participant().isInstructor()).isTrue();
    }

    @Test
    void un_equipo_no_puede_arrancar_la_partida_ni_resolver_la_ronda() {
        Game partida = partidaEnCurso();
        ParticipantSession equipo = sesionDe(partida, TeamId.A);

        assertThatThrownBy(() -> servicio.resolveCurrentRound(partida.id(), equipo))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> servicio.closeMatch(partida.id(), equipo))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> servicio.setAutoResolve(partida.id(), equipo, true))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void el_instructor_arbitra_pero_no_juega() {
        Game partida = partidaEnCurso();
        ParticipantSession instructor = sesionDe(partida, null);

        assertThatThrownBy(() -> servicio.enqueueAction(
                partida.id(), instructor, new EnqueueActionCommand(ATAQUE, Map.of())))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> servicio.markReady(partida.id(), instructor))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void un_token_de_otra_partida_no_sirve() {
        Game partida = partidaEnCurso();
        ParticipantSession deOtraPartida = new ParticipantSession(
                GameId.newId(), partida.playerOf(TeamId.A).orElseThrow());

        assertThatThrownBy(() -> servicio.getGameState(partida.id(), deOtraPartida))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void un_participante_que_no_esta_en_la_partida_no_entra_aunque_traiga_token() {
        Game partida = partidaEnCurso();
        ParticipantSession colado = new ParticipantSession(
                partida.id(), Participant.player(TeamId.A, "Impostor"));

        assertThatThrownBy(() -> servicio.getGameState(partida.id(), colado))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void el_bando_lo_pone_el_servidor_y_no_el_cliente() {
        Game partida = partidaEnCurso();

        servicio.enqueueAction(partida.id(), sesionDe(partida, TeamId.A), new EnqueueActionCommand(ATAQUE, Map.of()));

        assertThat(partida.currentRound().queuedActions())
                .singleElement()
                .satisfies(accion -> assertThat(accion.team()).isEqualTo(Role.ATTACKER));
    }

    @Test
    void no_se_puede_jugar_una_carta_del_bando_contrario() {
        Game partida = partidaEnCurso();

        assertThatThrownBy(() -> servicio.enqueueAction(
                partida.id(), sesionDe(partida, TeamId.A), new EnqueueActionCommand(DEFENSA, Map.of())))
                .isInstanceOf(UnknownCardException.class);
    }

    @Test
    void no_se_puede_encolar_sin_presupuesto() {
        Game partida = partidaEnCurso();
        ParticipantSession atacante = sesionDe(partida, TeamId.A);
        for (int i = 0; i < 10; i++) {
            servicio.enqueueAction(partida.id(), atacante, new EnqueueActionCommand(ATAQUE, Map.of()));
        }

        assertThatThrownBy(() -> servicio.enqueueAction(
                partida.id(), atacante, new EnqueueActionCommand(ATAQUE, Map.of())))
                .isInstanceOf(InsufficientBudgetException.class);
    }

    @Test
    void encolar_no_difunde_nada_porque_los_turnos_son_a_ciegas() {
        Game partida = partidaEnCurso();
        int antes = difusor.veces();

        servicio.enqueueAction(partida.id(), sesionDe(partida, TeamId.A), new EnqueueActionCommand(ATAQUE, Map.of()));

        assertThat(difusor.veces()).isEqualTo(antes);
    }

    @Test
    void con_el_modo_manual_confirmar_los_dos_no_cierra_la_ronda() {
        Game partida = partidaEnCurso();

        servicio.markReady(partida.id(), sesionDe(partida, TeamId.A));
        servicio.markReady(partida.id(), sesionDe(partida, TeamId.B));

        assertThat(partida.currentRound().number()).isEqualTo(1);
    }

    @Test
    void con_el_modo_automatico_confirmar_los_dos_cierra_la_ronda() {
        Game partida = partidaEnCurso();
        servicio.setAutoResolve(partida.id(), sesionDe(partida, null), true);

        servicio.markReady(partida.id(), sesionDe(partida, TeamId.A));
        servicio.markReady(partida.id(), sesionDe(partida, TeamId.B));

        assertThat(partida.currentRound().number()).isEqualTo(2);
    }

    @Test
    void el_reloj_no_toca_las_partidas_en_modo_manual() {
        partidaEnCurso();

        assertThat(servicio.resolveExpiredRounds()).isZero();
    }

    @Test
    void unirse_a_una_partida_llena_no_cuela() {
        GameAccess acceso = servicio.createGame(Partidas.AJUSTES);
        servicio.joinGame(acceso.joinCode(), "Rojos");
        servicio.joinGame(acceso.joinCode(), "Azules");

        assertThatThrownBy(() -> servicio.joinGame(acceso.joinCode(), "Sobra"))
                .isInstanceOf(com.hackdeck.domain.exception.GameNotJoinableException.class);
    }

    @Test
    void los_codigos_de_dos_partidas_no_se_repiten() {
        JoinCode primera = servicio.createGame(Partidas.AJUSTES).joinCode();
        JoinCode segunda = servicio.createGame(Partidas.AJUSTES).joinCode();

        assertThat(primera).isNotEqualTo(segunda);
    }
}
