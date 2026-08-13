package com.hackdeck.domain.model;

import com.hackdeck.apoyo.Partidas;
import com.hackdeck.domain.catalog.KillChainPhase;
import com.hackdeck.domain.exception.InsufficientBudgetException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HalfTest {

    private static final GameSettings AJUSTES = new GameSettings(6, Duration.ofSeconds(60), 20, 10);

    private Half mitad() {
        return new Half(Half.FIRST, TeamId.A, AJUSTES);
    }

    @Test
    void lo_que_no_se_gasta_se_acumula_para_la_ronda_siguiente() {
        Half mitad = mitad();
        mitad.spend(TeamId.A, 5);

        mitad.advanceRound(Map.of());

        assertThat(mitad.budgetOf(TeamId.A)).isEqualTo(20 - 5 + 10);
    }

    @Test
    void el_bonus_de_catch_up_se_suma_al_ingreso_normal() {
        Half mitad = mitad();

        mitad.advanceRound(Map.of(TeamId.A, 7));

        assertThat(mitad.budgetOf(TeamId.A)).isEqualTo(20 + 10 + 7);
        assertThat(mitad.budgetOf(TeamId.B)).isEqualTo(20 + 10);
    }

    @Test
    void no_se_puede_gastar_lo_que_no_se_tiene() {
        Half mitad = mitad();

        assertThatThrownBy(() -> mitad.spend(TeamId.A, 21)).isInstanceOf(InsufficientBudgetException.class);
        assertThat(mitad.budgetOf(TeamId.A)).isEqualTo(20);
    }

    @Test
    void el_atacante_empieza_solo_con_el_reconocimiento_abierto() {
        Half mitad = mitad();

        assertThat(mitad.isUnlocked(KillChainPhase.RECON)).isTrue();
        assertThat(mitad.isUnlocked(KillChainPhase.ACCESS)).isFalse();
    }

    @Test
    void una_carta_temporal_caduca_y_una_permanente_no() {
        Half mitad = mitad();
        mitad.activate(new ActiveCard("temporal", Role.DEFENDER, 1));
        mitad.activate(ActiveCard.permanent("permanente", Role.DEFENDER));

        mitad.advanceRound(Map.of());

        assertThat(mitad.isActive("temporal")).isFalse();
        assertThat(mitad.isActive("permanente")).isTrue();
    }

    @Test
    void la_ronda_se_cierra_sola_solo_cuando_han_confirmado_los_dos() {
        Half mitad = mitad();

        mitad.currentRound().markReady(TeamId.A);
        assertThat(mitad.currentRound().everyoneReady()).isFalse();

        mitad.currentRound().markReady(TeamId.B);
        assertThat(mitad.currentRound().everyoneReady()).isTrue();
    }

    @Test
    void al_avanzar_de_ronda_las_confirmaciones_se_olvidan() {
        Half mitad = mitad();
        mitad.currentRound().markReady(TeamId.A);

        mitad.advanceRound(Map.of());

        assertThat(mitad.currentRound().readyTeams()).isEmpty();
    }

    @Test
    void la_triada_defendida_es_la_suma_de_los_tres_pilares() {
        Half mitad = mitad();
        mitad.applyResolvedState(CiaState.intact().withImpact(CiaPillar.AVAILABILITY, -30));

        assertThat(mitad.defendedCia()).isEqualTo(100 + 100 + 70);
    }

    @Test
    void los_ajustes_rechazan_un_tiempo_de_ronda_absurdo() {
        assertThatThrownBy(() -> new GameSettings(6, Duration.ofSeconds(2), 20, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
