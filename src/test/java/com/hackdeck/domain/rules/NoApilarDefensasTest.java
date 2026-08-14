package com.hackdeck.domain.rules;

import com.hackdeck.apoyo.AzarControlado;
import com.hackdeck.apoyo.Cartas;
import com.hackdeck.apoyo.Partidas;
import com.hackdeck.domain.catalog.ActionCatalog;
import com.hackdeck.domain.catalog.CardDuration;
import com.hackdeck.domain.exception.AlreadyDeployedException;
import com.hackdeck.domain.model.ActiveCard;
import com.hackdeck.domain.model.CiaPillar;
import com.hackdeck.domain.model.Game;
import com.hackdeck.domain.model.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repetir una capa no la refuerza: si lo hiciera, la jugada optima del
 * defensor seria comprar tres veces la misma carta en vez de leer la amenaza.
 */
class NoApilarDefensasTest {

    private static final String PERMANENTE = "permanente";
    private static final String POR_RONDAS = "por-rondas";
    private static final String INSTANTANEA = "instantanea";
    private static final String ATAQUE = "ataque";

    @Test
    void una_defensa_permanente_ya_desplegada_no_se_puede_volver_a_encolar() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.defensa(PERMANENTE).dura(CardDuration.PERMANENT, 0).mitiga(10).build());
        DefaultRuleEngine motor = new DefaultRuleEngine(catalogo, AzarControlado.siempre());
        Game partida = Partidas.enCurso();
        partida.currentHalf().activate(ActiveCard.permanent(PERMANENTE, Role.DEFENDER));

        assertThatThrownBy(() -> motor.playableCard(partida, Role.DEFENDER, PERMANENTE))
                .isInstanceOf(AlreadyDeployedException.class);
    }

    @Test
    void la_misma_defensa_dos_veces_en_una_ronda_tampoco() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.defensa(POR_RONDAS).dura(CardDuration.ROUNDS, 2).build());
        DefaultRuleEngine motor = new DefaultRuleEngine(catalogo, AzarControlado.siempre());
        Game partida = Partidas.enCurso();
        Partidas.encola(partida, Role.DEFENDER, POR_RONDAS);

        assertThatThrownBy(() -> motor.playableCard(partida, Role.DEFENDER, POR_RONDAS))
                .isInstanceOf(AlreadyDeployedException.class);
    }

    @Test
    void una_defensa_por_rondas_si_se_renueva_cuando_esta_a_punto_de_caducar() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.defensa(POR_RONDAS).dura(CardDuration.ROUNDS, 2).build());
        DefaultRuleEngine motor = new DefaultRuleEngine(catalogo, AzarControlado.siempre());
        Game partida = Partidas.enCurso();
        partida.currentHalf().activate(new ActiveCard(POR_RONDAS, Role.DEFENDER, 1));

        assertThatCode(() -> motor.playableCard(partida, Role.DEFENDER, POR_RONDAS))
                .doesNotThrowAnyException();
    }

    @Test
    void una_defensa_instantanea_se_puede_repetir_porque_cada_uso_se_paga() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.defensa(INSTANTANEA).impacto(CiaPillar.AVAILABILITY, 20).build());
        DefaultRuleEngine motor = new DefaultRuleEngine(catalogo, AzarControlado.siempre());
        Game partida = Partidas.enCurso();
        Partidas.encola(partida, Role.DEFENDER, INSTANTANEA);

        assertThatCode(() -> motor.playableCard(partida, Role.DEFENDER, INSTANTANEA))
                .doesNotThrowAnyException();
    }

    @Test
    void el_atacante_si_puede_repetir_su_carta_porque_vuelve_a_hacer_dano() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque(ATAQUE)
                        .dura(CardDuration.PERMANENT, 0)
                        .impacto(CiaPillar.AVAILABILITY, -10)
                        .build());
        DefaultRuleEngine motor = new DefaultRuleEngine(catalogo, AzarControlado.siempre());
        Game partida = Partidas.enCurso();
        Partidas.encola(partida, Role.ATTACKER, ATAQUE);
        partida.currentHalf().activate(ActiveCard.permanent(ATAQUE, Role.ATTACKER));

        assertThatCode(() -> motor.playableCard(partida, Role.ATTACKER, ATAQUE))
                .doesNotThrowAnyException();
    }

    @Test
    void desplegar_dos_veces_la_misma_capa_no_duplica_su_mitigacion() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.defensa(PERMANENTE).dura(CardDuration.PERMANENT, 0).mitiga(10).build(),
                Cartas.ataque(ATAQUE).impacto(CiaPillar.AVAILABILITY, -30).build());
        Game partida = Partidas.enCurso();
        partida.currentHalf().activate(ActiveCard.permanent(PERMANENTE, Role.DEFENDER));
        partida.currentHalf().activate(ActiveCard.permanent(PERMANENTE, Role.DEFENDER));
        Partidas.encola(partida, Role.ATTACKER, ATAQUE);

        RoundResolution resolucion = new DefaultRuleEngine(catalogo, AzarControlado.siempre())
                .resolveRound(partida, partida.currentRound());

        assertThat(resolucion.resultingState().levelOf(CiaPillar.AVAILABILITY)).isEqualTo(100 - (30 - 10));
    }
}
