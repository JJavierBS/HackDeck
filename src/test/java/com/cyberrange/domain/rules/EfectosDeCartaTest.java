package com.cyberrange.domain.rules;

import com.cyberrange.apoyo.AzarControlado;
import com.cyberrange.apoyo.Cartas;
import com.cyberrange.apoyo.Partidas;
import com.cyberrange.domain.catalog.ActionCatalog;
import com.cyberrange.domain.catalog.CardDuration;
import com.cyberrange.domain.catalog.CardEffect;
import com.cyberrange.domain.catalog.DefenseCategory;
import com.cyberrange.domain.catalog.KillChainPhase;
import com.cyberrange.domain.catalog.NoiseLevel;
import com.cyberrange.domain.exception.MissingRequirementException;
import com.cyberrange.domain.model.ActiveCard;
import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameEvent;
import com.cyberrange.domain.model.GameEventType;
import com.cyberrange.domain.model.Role;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Los efectos que una carta promete en su texto tienen que ocurrir: en un
 * juego para aprender, prometer y no cumplir es peor que no tener la carta.
 */
class EfectosDeCartaTest {

    private RoundResolution resolver(Game partida, ActionCatalog catalogo, AzarControlado azar) {
        return new DefaultRuleEngine(catalogo, azar).resolveRound(partida, partida.currentRound());
    }

    @Test
    void un_reconocimiento_que_revela_defensas_deja_al_atacante_verlas() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque("escaneo").fase(KillChainPhase.RECON)
                        .conEfecto(CardEffect.REVEALS_DEFENCES).build());
        Game partida = Partidas.enCurso();
        Partidas.encola(partida, Role.ATTACKER, "escaneo");
        assertThat(partida.currentHalf().areDefencesRevealed()).isFalse();

        resolver(partida, catalogo, AzarControlado.siempre());

        assertThat(partida.currentHalf().areDefencesRevealed()).isTrue();
    }

    @Test
    void si_el_escaneo_falla_no_se_revela_nada() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque("escaneo").fase(KillChainPhase.RECON).acierto(0.5)
                        .conEfecto(CardEffect.REVEALS_DEFENCES).build());
        Game partida = Partidas.enCurso();
        Partidas.encola(partida, Role.ATTACKER, "escaneo");

        resolver(partida, catalogo, AzarControlado.nunca());

        assertThat(partida.currentHalf().areDefencesRevealed()).isFalse();
    }

    @Test
    void revisar_los_registros_descubre_lo_que_en_su_momento_no_se_detecto() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque("sigiloso").fase(KillChainPhase.RECON).ruido(NoiseLevel.LOW).build(),
                Cartas.defensa("logs").categoria(DefenseCategory.DETECTION)
                        .conEfecto(CardEffect.REVEALS_PREVIOUS_ROUND).build());
        Game partida = Partidas.enCurso();
        DefaultRuleEngine motor = new DefaultRuleEngine(catalogo, AzarControlado.nunca());

        Partidas.encola(partida, Role.ATTACKER, "sigiloso");
        RoundResolution primera = motor.resolveRound(partida, partida.currentRound());
        partida.applyRoundResolution(
                primera.resultingState(), primera.generatedEvents(), false, Map.of(), false);
        assertThat(ataqueDelHistorial(partida).visibleToDefender()).isFalse();

        Partidas.encola(partida, Role.DEFENDER, "logs");
        RoundResolution segunda = motor.resolveRound(partida, partida.currentRound());
        partida.applyRoundResolution(
                segunda.resultingState(), segunda.generatedEvents(), false, Map.of(),
                segunda.revealsPreviousRound());

        assertThat(ataqueDelHistorial(partida).visibleToDefender()).isTrue();
    }

    @Test
    void el_aviso_del_cert_anula_el_ataque_elegido() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque("ransomware").fase(KillChainPhase.RECON).acierto(1.0)
                        .impacto(CiaPillar.AVAILABILITY, -50).build(),
                Cartas.defensa("cert").conEfecto(CardEffect.BLOCKS_CHOSEN_ATTACK).build());
        Game partida = Partidas.enCurso();
        partida.currentHalf().blockAttack("ransomware", "cert");
        Partidas.encola(partida, Role.ATTACKER, "ransomware");

        RoundResolution resultado = resolver(partida, catalogo, AzarControlado.siempre());

        assertThat(resultado.resultingState().levelOf(CiaPillar.AVAILABILITY)).isEqualTo(100);
        assertThat(ataqueDe(resultado).detail().success()).isFalse();
        assertThat(ataqueDe(resultado).detail().counteredBy()).isEqualTo("cert");
        assertThat(ataqueDe(resultado).detail().failureReason())
                .isEqualTo(com.cyberrange.domain.model.FailureReason.COUNTERED);
    }

    @Test
    void el_aviso_del_cert_se_gasta_al_usarlo() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque("ransomware").fase(KillChainPhase.RECON).acierto(1.0)
                        .impacto(CiaPillar.AVAILABILITY, -50).build());
        Game partida = Partidas.enCurso();
        partida.currentHalf().blockAttack("ransomware", "cert");
        Partidas.encola(partida, Role.ATTACKER, "ransomware");
        resolver(partida, catalogo, AzarControlado.siempre());

        assertThat(partida.currentHalf().isBlocked("ransomware")).isFalse();
    }

    @Test
    void el_teletrabajo_deja_las_capas_de_perimetro_a_la_mitad() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.ataque("golpe").fase(KillChainPhase.RECON).impacto(CiaPillar.AVAILABILITY, -40).build(),
                Cartas.defensa("firewall").categoria(DefenseCategory.ARCHITECTURE).mitiga(10).build(),
                Cartas.defensa("teletrabajo").conEfecto(CardEffect.WEAKENS_PERIMETER)
                        .dura(CardDuration.ROUNDS, 3).build());

        Game normal = Partidas.enCurso();
        normal.currentHalf().activate(ActiveCard.permanent("firewall", Role.DEFENDER));
        Partidas.encola(normal, Role.ATTACKER, "golpe");
        int conFirewall = resolver(normal, catalogo, AzarControlado.siempre())
                .resultingState().levelOf(CiaPillar.AVAILABILITY);

        Game teletrabajando = Partidas.enCurso();
        teletrabajando.currentHalf().activate(ActiveCard.permanent("firewall", Role.DEFENDER));
        teletrabajando.currentHalf().activate(new ActiveCard("teletrabajo", null, 3));
        Partidas.encola(teletrabajando, Role.ATTACKER, "golpe");
        int conTeletrabajo = resolver(teletrabajando, catalogo, AzarControlado.siempre())
                .resultingState().levelOf(CiaPillar.AVAILABILITY);

        assertThat(conFirewall).isEqualTo(100 - 30);
        assertThat(conTeletrabajo).isEqualTo(100 - 35);
    }

    @Test
    void una_carta_que_exige_otra_no_se_puede_jugar_sin_ella() {
        ActionCatalog catalogo = Partidas.catalogo(
                Cartas.defensa("restaurar").necesita("copias").build(),
                Cartas.defensa("copias").build());
        Game partida = Partidas.enCurso();
        DefaultRuleEngine motor = new DefaultRuleEngine(catalogo, AzarControlado.siempre());

        assertThatThrownBy(() -> motor.playableCard(partida, Role.DEFENDER, "restaurar"))
                .isInstanceOf(MissingRequirementException.class)
                .hasMessageContaining("copias");

        partida.currentHalf().activate(ActiveCard.permanent("copias", Role.DEFENDER));
        assertThat(motor.playableCard(partida, Role.DEFENDER, "restaurar").id()).isEqualTo("restaurar");
    }

    private static GameEvent ataqueDe(RoundResolution resultado) {
        return resultado.generatedEvents().stream()
                .filter(evento -> evento.type() == GameEventType.ATTACK)
                .findFirst()
                .orElseThrow();
    }

    private static GameEvent ataqueDelHistorial(Game partida) {
        return partida.history().stream()
                .filter(evento -> evento.type() == GameEventType.ATTACK)
                .findFirst()
                .orElseThrow();
    }
}
