package com.hackdeck.domain.rules;

import com.hackdeck.apoyo.AzarControlado;
import com.hackdeck.apoyo.Partidas;
import com.hackdeck.domain.model.CiaPillar;
import com.hackdeck.domain.model.CiaState;
import com.hackdeck.domain.model.Game;
import com.hackdeck.domain.model.MatchOutcome;
import com.hackdeck.domain.model.MatchResult;
import com.hackdeck.domain.model.TeamId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MarcadorTest {

    private final DefaultRuleEngine motor =
            new DefaultRuleEngine(Partidas.catalogo(), AzarControlado.siempre());

    private static final CiaState DERRIBADA = CiaState.intact().withImpact(CiaPillar.AVAILABILITY, -100);

    @Test
    void si_solo_uno_derriba_gana_el() {
        Game partida = Partidas.enCurso();
        derribar(partida);
        agotarMitad(partida, CiaState.intact());

        MatchResult resultado = motor.scoreMatch(partida);

        assertThat(resultado.winner()).isEqualTo(TeamId.A);
        assertThat(resultado.outcome()).isEqualTo(MatchOutcome.TAKEDOWN);
    }

    @Test
    void si_derriban_los_dos_gana_quien_tardo_menos_rondas() {
        Game partida = Partidas.enCurso();
        derribar(partida);
        partida.applyRoundResolution(CiaState.intact(), List.of(), false, Map.of(), false);
        derribar(partida);

        MatchResult resultado = motor.scoreMatch(partida);

        assertThat(resultado.winner()).isEqualTo(TeamId.A);
        assertThat(resultado.outcome()).isEqualTo(MatchOutcome.TAKEDOWN_FASTER);
        assertThat(resultado.takedownRound()).containsEntry(TeamId.A, 1).containsEntry(TeamId.B, 2);
    }

    @Test
    void si_derriban_los_dos_en_la_misma_ronda_se_decide_por_la_triada_defendida() {
        Game partida = Partidas.enCurso();
        partida.applyRoundResolution(
                CiaState.intact().withImpact(CiaPillar.CONFIDENTIALITY, -100).withImpact(CiaPillar.INTEGRITY, -50),
                List.of(), true, Map.of(), false);
        derribar(partida);

        MatchResult resultado = motor.scoreMatch(partida);

        assertThat(resultado.winner()).isEqualTo(TeamId.A);
        assertThat(resultado.outcome()).isEqualTo(MatchOutcome.POINTS);
    }

    @Test
    void sin_derribos_gana_quien_defendio_mejor() {
        Game partida = Partidas.enCurso();
        agotarMitad(partida, CiaState.intact().withImpact(CiaPillar.AVAILABILITY, -60));
        agotarMitad(partida, CiaState.intact().withImpact(CiaPillar.AVAILABILITY, -10));

        MatchResult resultado = motor.scoreMatch(partida);

        assertThat(resultado.outcome()).isEqualTo(MatchOutcome.POINTS);
        assertThat(resultado.winner()).isEqualTo(TeamId.A);
        assertThat(resultado.defendedCia()).containsEntry(TeamId.A, 290).containsEntry(TeamId.B, 240);
    }

    @Test
    void con_las_dos_mitades_igual_de_defendidas_hay_empate() {
        Game partida = Partidas.enCurso();
        agotarMitad(partida, CiaState.intact());
        agotarMitad(partida, CiaState.intact());

        MatchResult resultado = motor.scoreMatch(partida);

        assertThat(resultado.outcome()).isEqualTo(MatchOutcome.DRAW);
        assertThat(resultado.winner()).isNull();
    }

    private static void derribar(Game partida) {
        partida.applyRoundResolution(DERRIBADA, List.of(), true, Map.of(), false);
    }

    private static void agotarMitad(Game partida, CiaState estadoFinal) {
        for (int ronda = 0; ronda < Partidas.AJUSTES.roundsPerHalf(); ronda++) {
            partida.applyRoundResolution(estadoFinal, List.of(), false, Map.of(), false);
        }
    }
}
