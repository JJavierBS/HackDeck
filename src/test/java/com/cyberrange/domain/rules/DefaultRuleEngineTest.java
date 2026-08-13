package com.cyberrange.domain.rules;

import com.cyberrange.apoyo.AzarControlado;
import com.cyberrange.apoyo.Cartas;
import com.cyberrange.apoyo.Partidas;
import com.cyberrange.domain.catalog.ActionCatalog;
import com.cyberrange.domain.catalog.CardDuration;
import com.cyberrange.domain.catalog.KillChainPhase;
import com.cyberrange.domain.catalog.NoiseLevel;
import com.cyberrange.domain.exception.UnknownCardException;
import com.cyberrange.domain.model.ActiveCard;
import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameEvent;
import com.cyberrange.domain.model.GameEventType;
import com.cyberrange.domain.model.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class DefaultRuleEngineTest {

    private static final String ATAQUE = "ataque";
    private static final String DEFENSA = "defensa";

    private RoundResolution resolver(Game partida, ActionCatalog catalogo, AzarControlado azar) {
        return new DefaultRuleEngine(catalogo, azar).resolveRound(partida, partida.currentRound());
    }

    @Test
    void una_carta_del_bando_contrario_no_se_puede_jugar() {
        ActionCatalog catalogo = Partidas.catalogo(Cartas.defensa(DEFENSA).build());
        DefaultRuleEngine motor = new DefaultRuleEngine(catalogo, AzarControlado.siempre());

        assertThatThrownBy(() -> motor.cardFor(Role.ATTACKER, DEFENSA)).isInstanceOf(UnknownCardException.class);
        assertThatThrownBy(() -> motor.cardFor(Role.ATTACKER, "no-existe")).isInstanceOf(UnknownCardException.class);
    }

    @Test
    void atacar_sin_haber_abierto_la_fase_previa_cuesta_la_penalizacion_de_kill_chain() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque(ATAQUE).fase(KillChainPhase.IMPACT).acierto(0.8).build());
        Game partida = Partidas.enCurso();
        Partidas.encola(partida, Role.ATTACKER, ATAQUE);
        AzarControlado azar = AzarControlado.nunca();

        resolver(partida, catalogo, azar);

        assertThat(azar.primeraPreguntada()).isEqualTo(0.8 * DefaultRuleEngine.KILL_CHAIN_PENALTY, within(0.0001));
    }

    @Test
    void con_la_fase_abierta_el_acierto_es_el_de_la_carta() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque(ATAQUE).fase(KillChainPhase.RECON).acierto(0.8).build());
        Game partida = Partidas.enCurso();
        Partidas.encola(partida, Role.ATTACKER, ATAQUE);
        AzarControlado azar = AzarControlado.nunca();

        resolver(partida, catalogo, azar);

        assertThat(azar.primeraPreguntada()).isEqualTo(0.8, within(0.0001));
    }

    @Test
    void una_carta_de_reconocimiento_acertada_abre_la_fase_siguiente_y_mejora_a_otra() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque("recon").fase(KillChainPhase.RECON)
                        .desbloquea(KillChainPhase.ACCESS).mejora(ATAQUE, 0.3).build(),
                Cartas.ataque(ATAQUE).fase(KillChainPhase.ACCESS).acierto(0.5).build());
        Game partida = Partidas.enCurso();
        Partidas.encola(partida, Role.ATTACKER, "recon");
        Partidas.encola(partida, Role.ATTACKER, ATAQUE);
        AzarControlado azar = AzarControlado.siempre();

        resolver(partida, catalogo, azar);

        assertThat(partida.currentHalf().isUnlocked(KillChainPhase.ACCESS)).isTrue();
        // Solo hay dos tiradas: el ruido NONE ni siquiera consulta la suerte.
        assertThat(azar.preguntadas().get(1)).isEqualTo(0.8, within(0.0001));
    }

    @Test
    void contra_una_carta_que_no_es_de_impacto_el_counter_recorta_la_probabilidad() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque(ATAQUE).fase(KillChainPhase.RECON).acierto(0.8).build(),
                Cartas.defensa(DEFENSA).contrarresta(ATAQUE, 0.25).build());
        Game partida = Partidas.enCurso();
        partida.currentHalf().activate(ActiveCard.permanent(DEFENSA, Role.DEFENDER));
        Partidas.encola(partida, Role.ATTACKER, ATAQUE);
        AzarControlado azar = AzarControlado.nunca();

        resolver(partida, catalogo, azar);

        assertThat(azar.primeraPreguntada()).isEqualTo(0.8 * 0.25, within(0.0001));
    }

    @Test
    void contra_un_impacto_el_counter_recorta_el_dano_y_no_la_probabilidad() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque(ATAQUE).fase(KillChainPhase.IMPACT).acierto(1.0)
                        .impacto(CiaPillar.AVAILABILITY, -40).build(),
                Cartas.defensa(DEFENSA).contrarresta(ATAQUE, 0.5).build());
        Game partida = Partidas.enCurso();
        partida.currentHalf().unlock(KillChainPhase.IMPACT);
        partida.currentHalf().activate(ActiveCard.permanent(DEFENSA, Role.DEFENDER));
        Partidas.encola(partida, Role.ATTACKER, ATAQUE);
        AzarControlado azar = AzarControlado.siempre();

        RoundResolution resultado = resolver(partida, catalogo, azar);

        assertThat(azar.primeraPreguntada()).isEqualTo(1.0);
        assertThat(resultado.resultingState().levelOf(CiaPillar.AVAILABILITY)).isEqualTo(100 - 20);
    }

    @Test
    void la_mitigacion_resta_dano_pero_siempre_atraviesa_un_minimo() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque(ATAQUE).fase(KillChainPhase.IMPACT).impacto(CiaPillar.INTEGRITY, -5).build(),
                Cartas.defensa(DEFENSA).mitiga(50).build());
        Game partida = Partidas.enCurso();
        partida.currentHalf().unlock(KillChainPhase.IMPACT);
        partida.currentHalf().activate(ActiveCard.permanent(DEFENSA, Role.DEFENDER));
        Partidas.encola(partida, Role.ATTACKER, ATAQUE);

        RoundResolution resultado = resolver(partida, catalogo, AzarControlado.siempre());

        assertThat(resultado.resultingState().levelOf(CiaPillar.INTEGRITY))
                .isEqualTo(100 - DefaultRuleEngine.MIN_DAMAGE);
    }

    @Test
    void las_defensas_se_resuelven_antes_que_los_ataques_de_la_misma_ronda() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque(ATAQUE).fase(KillChainPhase.IMPACT).impacto(CiaPillar.AVAILABILITY, -30).build(),
                Cartas.defensa(DEFENSA).mitiga(10).dura(CardDuration.PERMANENT, 0).build());
        Game partida = Partidas.enCurso();
        partida.currentHalf().unlock(KillChainPhase.IMPACT);
        Partidas.encola(partida, Role.ATTACKER, ATAQUE);
        Partidas.encola(partida, Role.DEFENDER, DEFENSA);

        RoundResolution resultado = resolver(partida, catalogo, AzarControlado.siempre());

        assertThat(resultado.resultingState().levelOf(CiaPillar.AVAILABILITY)).isEqualTo(100 - 20);
    }

    @Test
    void lo_que_no_hace_ruido_no_se_detecta_ni_con_toda_la_deteccion_del_mundo() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque(ATAQUE).fase(KillChainPhase.RECON).ruido(NoiseLevel.NONE).build(),
                Cartas.defensa(DEFENSA).detecta(10).build());
        Game partida = Partidas.enCurso();
        partida.currentHalf().activate(ActiveCard.permanent(DEFENSA, Role.DEFENDER));
        Partidas.encola(partida, Role.ATTACKER, ATAQUE);

        RoundResolution resultado = resolver(partida, catalogo, AzarControlado.siempre());

        assertThat(ataqueDe(resultado).visibleToDefender()).isFalse();
    }

    @Test
    void un_ataque_escandaloso_se_ve_aunque_el_defensor_no_tenga_deteccion() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque(ATAQUE).fase(KillChainPhase.RECON).ruido(NoiseLevel.HIGH).build());
        Game partida = Partidas.enCurso();
        Partidas.encola(partida, Role.ATTACKER, ATAQUE);

        RoundResolution resultado = resolver(partida, catalogo, AzarControlado.nunca());

        assertThat(ataqueDe(resultado).visibleToDefender()).isTrue();
    }

    @Test
    void un_ataque_sigiloso_solo_se_ve_si_hay_con_que_mirar() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque(ATAQUE).fase(KillChainPhase.RECON).ruido(NoiseLevel.LOW).build(),
                Cartas.defensa(DEFENSA).detecta(2).build());

        Game sinIds = Partidas.enCurso();
        Partidas.encola(sinIds, Role.ATTACKER, ATAQUE);
        assertThat(ataqueDe(resolver(sinIds, catalogo, AzarControlado.nunca())).visibleToDefender()).isFalse();

        Game conIds = Partidas.enCurso();
        conIds.currentHalf().activate(ActiveCard.permanent(DEFENSA, Role.DEFENDER));
        Partidas.encola(conIds, Role.ATTACKER, ATAQUE);
        assertThat(ataqueDe(resolver(conIds, catalogo, AzarControlado.nunca())).visibleToDefender()).isTrue();
    }

    private static GameEvent ataqueDe(RoundResolution resultado) {
        return resultado.generatedEvents().stream()
                .filter(evento -> evento.type() == GameEventType.ATTACK)
                .findFirst()
                .orElseThrow();
    }
}
