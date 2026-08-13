package com.cyberrange.domain.model;

import com.cyberrange.apoyo.Partidas;
import com.cyberrange.domain.exception.GameNotJoinableException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    private static final CiaState TOCADA = CiaState.intact().withImpact(CiaPillar.AVAILABILITY, -40);
    private static final CiaState DERRIBADA = CiaState.intact().withImpact(CiaPillar.AVAILABILITY, -100);

    @Test
    void los_equipos_se_reparten_por_orden_de_llegada_y_no_caben_tres() {
        Game partida = Partidas.enPreparacion(Partidas.AJUSTES);

        assertThat(partida.players().keySet()).containsExactly(TeamId.A, TeamId.B);
        assertThat(partida.firstFreeTeam()).isEmpty();
        assertThatThrownBy(() -> partida.join(Participant.player(TeamId.A, "Sobra")))
                .isInstanceOf(GameNotJoinableException.class);
    }

    @Test
    void no_se_puede_empezar_sin_los_dos_equipos() {
        Game partida = Game.create(JoinCode.generate(), Participant.instructor(), Partidas.AJUSTES);
        partida.join(Participant.player(TeamId.A, "Solos"));

        assertThatThrownBy(partida::startMatch).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void en_la_segunda_mitad_cambian_los_bandos_y_la_triada_vuelve_a_estar_intacta() {
        Game partida = Partidas.enCurso();
        assertThat(partida.sideOf(TeamId.A)).isEqualTo(Role.ATTACKER);

        terminarMitad(partida);

        assertThat(partida.currentHalf().number()).isEqualTo(Half.SECOND);
        assertThat(partida.sideOf(TeamId.A)).isEqualTo(Role.DEFENDER);
        assertThat(partida.sideOf(TeamId.B)).isEqualTo(Role.ATTACKER);
        assertThat(partida.ciaState().levelOf(CiaPillar.AVAILABILITY)).isEqualTo(100);
    }

    @Test
    void derribar_un_pilar_cierra_la_mitad_en_el_acto_aunque_queden_rondas() {
        Game partida = Partidas.enCurso();

        partida.applyRoundResolution(DERRIBADA, List.of(), true, Map.of());

        assertThat(partida.currentHalf().number()).isEqualTo(Half.SECOND);
        assertThat(partida.halves().getFirst().takedownRound()).isEqualTo(1);
    }

    @Test
    void el_presupuesto_de_la_mitad_anterior_no_se_arrastra() {
        Game partida = Partidas.enCurso();
        partida.currentHalf().spend(TeamId.A, 60);

        terminarMitad(partida);

        assertThat(partida.currentHalf().budgetOf(TeamId.A)).isEqualTo(Partidas.AJUSTES.initialBudget());
    }

    @Test
    void con_la_partida_terminada_ya_no_se_encolan_acciones() {
        Game partida = Partidas.enCurso();
        partida.closeMatch();

        assertThatThrownBy(() -> Partidas.encola(partida, Role.ATTACKER, "lo-que-sea"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void el_historial_recoge_tambien_lo_que_no_es_una_jugada() {
        Game partida = Partidas.enCurso();

        assertThat(partida.history())
                .extracting(GameEvent::type)
                .contains(GameEventType.TEAM_JOINED, GameEventType.MATCH_STARTED, GameEventType.HALF_STARTED);
    }

    @Test
    void el_historial_no_se_pierde_al_cambiar_de_mitad() {
        Game partida = Partidas.enCurso();
        int antes = partida.history().size();

        terminarMitad(partida);

        assertThat(partida.history()).hasSizeGreaterThan(antes);
        assertThat(partida.history()).anyMatch(evento -> evento.halfNumber() == Half.FIRST);
    }

    private static void terminarMitad(Game partida) {
        for (int ronda = 0; ronda < Partidas.AJUSTES.roundsPerHalf(); ronda++) {
            partida.applyRoundResolution(TOCADA, List.of(), false, Map.of());
        }
    }
}
