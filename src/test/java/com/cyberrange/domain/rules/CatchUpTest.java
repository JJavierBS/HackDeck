package com.cyberrange.domain.rules;

import com.cyberrange.apoyo.AzarControlado;
import com.cyberrange.apoyo.Cartas;
import com.cyberrange.apoyo.Partidas;
import com.cyberrange.domain.catalog.KillChainPhase;
import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.Role;
import com.cyberrange.domain.model.TeamId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El bando que va por detras de lo esperado a esas alturas recibe
 * presupuesto extra, para que una partida perdida no se vuelva aburrida.
 */
class CatchUpTest {

    @Test
    void si_la_triada_aguanta_mas_de_lo_normal_la_ayuda_es_para_el_atacante() {
        Game partida = Partidas.enCurso();

        var resultado = new DefaultRuleEngine(Partidas.catalogo(), AzarControlado.siempre())
                .resolveRound(partida, partida.currentRound());

        assertThat(resultado.catchUpBonus()).containsOnlyKeys(partida.currentHalf().attackingTeam());
    }

    @Test
    void si_la_triada_se_desploma_la_ayuda_es_para_el_defensor() {
        var catalogo = Partidas.catalogo(
                Cartas.ataque("demoledor").fase(KillChainPhase.IMPACT)
                        .impacto(CiaPillar.AVAILABILITY, -90).build());
        Game partida = Partidas.enCurso();
        partida.currentHalf().unlock(KillChainPhase.IMPACT);
        Partidas.encola(partida, Role.ATTACKER, "demoledor");

        var resultado = new DefaultRuleEngine(catalogo, AzarControlado.siempre())
                .resolveRound(partida, partida.currentRound());

        assertThat(resultado.catchUpBonus()).containsOnlyKeys(partida.currentHalf().defendingTeam());
    }

    @Test
    void la_ayuda_tiene_tope() {
        var catalogo = Partidas.catalogo(
                Cartas.ataque("demoledor").fase(KillChainPhase.IMPACT)
                        .impacto(CiaPillar.AVAILABILITY, -100).build());
        Game partida = Partidas.enCurso();
        partida.currentHalf().unlock(KillChainPhase.IMPACT);
        Partidas.encola(partida, Role.ATTACKER, "demoledor");

        var resultado = new DefaultRuleEngine(catalogo, AzarControlado.siempre())
                .resolveRound(partida, partida.currentRound());

        assertThat(resultado.catchUpBonus().values())
                .allMatch(bonus -> bonus <= DefaultRuleEngine.CATCH_UP_MAX);
    }

    @Test
    void el_presupuesto_extra_llega_de_verdad_al_equipo_que_va_perdiendo() {
        Game partida = Partidas.enCurso();
        int antes = partida.currentHalf().budgetOf(TeamId.A);

        var resultado = new DefaultRuleEngine(Partidas.catalogo(), AzarControlado.siempre())
                .resolveRound(partida, partida.currentRound());
        partida.applyRoundResolution(
                resultado.resultingState(), resultado.generatedEvents(), false, resultado.catchUpBonus(), false);

        assertThat(partida.currentHalf().budgetOf(TeamId.A))
                .isGreaterThan(antes + Partidas.AJUSTES.incomePerRound());
    }
}
