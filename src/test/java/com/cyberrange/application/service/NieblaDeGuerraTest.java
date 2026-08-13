package com.cyberrange.application.service;

import com.cyberrange.apoyo.Partidas;
import com.cyberrange.application.view.GameView;
import com.cyberrange.domain.catalog.KillChainPhase;
import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.EventDetail;
import com.cyberrange.domain.model.FailureReason;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameEvent;
import com.cyberrange.domain.model.GameEventType;
import com.cyberrange.domain.model.Participant;
import com.cyberrange.domain.model.Role;
import com.cyberrange.domain.model.TeamId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lo que un bando no debe saber no puede salir del servidor. Estos son los
 * tests que evitan que una fuga pase inadvertida al tocar la proyeccion.
 */
class NieblaDeGuerraTest {

    private final GameViewProjector proyector = new GameViewProjector(Partidas::catalogoVacio);

    private Game partidaConAtaque(boolean detectado) {
        Game partida = Partidas.enCurso();
        Partidas.encola(partida, Role.ATTACKER, "ataque-secreto");
        Partidas.encola(partida, Role.DEFENDER, "defensa-propia");
        partida.record(GameEvent.byCard(
                1, 1, GameEventType.ATTACK, Role.ATTACKER, "ataque-secreto", "ataque", detectado,
                EventDetail.attack(
                        false,
                        FailureReason.COUNTERED,
                        Map.of(CiaPillar.AVAILABILITY, -10),
                        4,
                        List.of(KillChainPhase.IMPACT),
                        List.of("otra-carta"),
                        detectado,
                        "mfa")));
        return partida;
    }

    private GameView vista(Game partida, TeamId equipo) {
        return proyector.project(partida, partida.playerOf(equipo).orElseThrow());
    }

    @Test
    void las_defensas_del_rival_solo_se_ven_si_se_ha_pagado_por_averiguarlas() {
        Game partida = Partidas.enCurso();
        partida.currentHalf().activate(
                com.cyberrange.domain.model.ActiveCard.permanent("mfa", Role.DEFENDER));

        assertThat(vista(partida, TeamId.A).revealedRivalCards()).isEmpty();

        partida.currentHalf().revealDefences();
        assertThat(vista(partida, TeamId.A).revealedRivalCards())
                .singleElement()
                .satisfies(carta -> assertThat(carta.cardId()).isEqualTo("mfa"));
        assertThat(vista(partida, TeamId.B).revealedRivalCards()).isEmpty();
    }

    @Test
    void el_defensor_no_ve_los_ataques_que_no_ha_detectado() {
        GameView defensor = vista(partidaConAtaque(false), TeamId.B);

        assertThat(defensor.events()).noneMatch(evento -> "ATTACK".equals(evento.type()));
    }

    @Test
    void el_defensor_ve_los_que_si_detecta_y_con_que_carta_suya_los_freno() {
        GameView defensor = vista(partidaConAtaque(true), TeamId.B);

        assertThat(defensor.events())
                .filteredOn(evento -> "ATTACK".equals(evento.type()))
                .singleElement()
                .satisfies(evento -> assertThat(evento.detail().counteredBy()).isEqualTo("mfa"));
    }

    @Test
    void al_atacante_se_le_dice_que_algo_le_freno_pero_no_que_carta_era() {
        GameView atacante = vista(partidaConAtaque(true), TeamId.A);

        assertThat(atacante.events())
                .filteredOn(evento -> "ATTACK".equals(evento.type()))
                .singleElement()
                .satisfies(evento -> {
                    assertThat(evento.detail().failureReason()).isEqualTo("COUNTERED");
                    assertThat(evento.detail().counteredBy()).isNull();
                });
    }

    @Test
    void de_una_accion_del_rival_no_se_ensena_lo_que_a_el_le_aporto() {
        GameView defensor = vista(partidaConAtaque(true), TeamId.B);

        assertThat(defensor.events())
                .filteredOn(evento -> "ATTACK".equals(evento.type()))
                .singleElement()
                .satisfies(evento -> {
                    assertThat(evento.detail().unlocked()).isEmpty();
                    assertThat(evento.detail().boosts()).isEmpty();
                });
    }

    @Test
    void nadie_ve_la_cola_del_rival_antes_de_resolver() {
        Game partida = partidaConAtaque(false);

        assertThat(vista(partida, TeamId.A).yourQueuedActions())
                .singleElement()
                .satisfies(accion -> assertThat(accion.cardId()).isEqualTo("ataque-secreto"));
        assertThat(vista(partida, TeamId.B).yourQueuedActions())
                .singleElement()
                .satisfies(accion -> assertThat(accion.cardId()).isEqualTo("defensa-propia"));
    }

    @Test
    void el_presupuesto_del_rival_no_sale_del_servidor_pero_el_instructor_los_ve() {
        Game partida = partidaConAtaque(false);

        assertThat(vista(partida, TeamId.A).budgets()).isNull();
        assertThat(proyector.project(partida, partida.instructor()).budgets())
                .containsOnlyKeys(TeamId.A.name(), TeamId.B.name());
    }

    @Test
    void el_instructor_ve_las_dos_colas_porque_su_panel_ya_no_es_lo_que_se_proyecta() {
        GameView instructor = proyector.project(partidaConAtaque(false), partidaConAtaque(false).instructor());

        assertThat(instructor.queuedBySide()).containsOnlyKeys(Role.ATTACKER.name(), Role.DEFENDER.name());
    }

    @Test
    void la_kill_chain_solo_se_le_manda_a_quien_ataca() {
        Game partida = partidaConAtaque(false);

        assertThat(vista(partida, TeamId.A).yourKillChain()).isNotEmpty();
        assertThat(vista(partida, TeamId.B).yourKillChain()).isEmpty();
    }

    @Test
    void cada_bando_ve_solo_sus_propias_capas_activas() {
        Game partida = Partidas.enCurso();
        partida.currentHalf().activate(
                com.cyberrange.domain.model.ActiveCard.permanent("mi-defensa", Role.DEFENDER));

        assertThat(vista(partida, TeamId.B).yourActiveCards()).hasSize(1);
        assertThat(vista(partida, TeamId.A).yourActiveCards()).isEmpty();
    }

    @Test
    void el_historial_del_debriefing_no_filtra_nada_porque_ya_no_hay_partida() {
        Game partida = partidaConAtaque(false);

        var historial = proyector.history(partida);

        assertThat(historial.events())
                .filteredOn(evento -> "ATTACK".equals(evento.type()))
                .singleElement()
                .satisfies(evento -> assertThat(evento.detail().counteredBy()).isEqualTo("mfa"));
    }

    @Test
    void el_participante_de_una_partida_sin_empezar_no_recibe_datos_de_mitad() {
        Game partida = Partidas.enPreparacion(Partidas.AJUSTES);

        GameView vista = proyector.project(partida, partida.playerOf(TeamId.A).orElseThrow());

        assertThat(vista.halfNumber()).isNull();
        assertThat(vista.ciaLevels()).isNull();
        assertThat(vista.roundDeadlineAt()).isNull();
    }

    @Test
    void el_instructor_no_tiene_equipo_ni_bando() {
        Game partida = Partidas.enCurso();

        GameView vista = proyector.project(partida, Participant.instructor());

        assertThat(vista.yourTeam()).isNull();
        assertThat(vista.yourSide()).isNull();
        assertThat(vista.yourBudget()).isNull();
    }
}
