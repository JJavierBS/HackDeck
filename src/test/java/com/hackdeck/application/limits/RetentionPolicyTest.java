package com.hackdeck.application.limits;

import com.hackdeck.domain.model.GamePhase;
import com.hackdeck.domain.model.TournamentPhase;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RetentionPolicyTest {

    private static final Instant AHORA = Instant.parse("2026-08-31T10:00:00Z");

    private final RetentionPolicy politica = new RetentionPolicy(
            Duration.ofMinutes(30), Duration.ofHours(2), Duration.ofHours(2), 500, 100);

    @Test
    void un_lobby_abandonado_caduca_antes_que_una_partida_en_curso() {
        Instant haceUnaHora = AHORA.minus(Duration.ofHours(1));

        assertThat(politica.isPurgeable(GamePhase.PREPARATION, haceUnaHora, AHORA)).isTrue();
        assertThat(politica.isPurgeable(GamePhase.IN_PROGRESS, haceUnaHora, AHORA)).isFalse();
    }

    @Test
    void una_partida_con_actividad_reciente_no_se_toca() {
        Instant haceUnMinuto = AHORA.minus(Duration.ofMinutes(1));

        for (GamePhase fase : GamePhase.values()) {
            assertThat(politica.isPurgeable(fase, haceUnMinuto, AHORA)).isFalse();
        }
    }

    @Test
    void una_partida_terminada_aguanta_para_el_debriefing_y_luego_se_va() {
        assertThat(politica.isPurgeable(GamePhase.FINISHED, AHORA.minus(Duration.ofMinutes(90)), AHORA)).isFalse();
        assertThat(politica.isPurgeable(GamePhase.FINISHED, AHORA.minus(Duration.ofHours(3)), AHORA)).isTrue();
    }

    @Test
    void el_torneo_caduca_por_su_propia_fase() {
        Instant haceUnaHora = AHORA.minus(Duration.ofHours(1));

        assertThat(politica.isPurgeable(TournamentPhase.LOBBY, haceUnaHora, AHORA)).isTrue();
        assertThat(politica.isPurgeable(TournamentPhase.IN_PROGRESS, haceUnaHora, AHORA)).isFalse();
    }

    @Test
    void el_tope_de_partidas_vivas_se_alcanza_en_el_limite_no_despues() {
        assertThat(politica.gamesAreFull(499)).isFalse();
        assertThat(politica.gamesAreFull(500)).isTrue();
        assertThat(politica.tournamentsAreFull(100)).isTrue();
    }
}
