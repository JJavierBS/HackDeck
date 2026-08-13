package com.cyberdeck.domain.model;

import com.cyberdeck.apoyo.Partidas;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TournamentTest {

    private Tournament conEquipos(String... nombres) {
        Tournament torneo = Tournament.create(
                JoinCode.generate(), Participant.instructor(), Partidas.AJUSTES);
        for (String nombre : nombres) {
            torneo.join(nombre);
        }
        return torneo;
    }

    @Test
    void empareja_por_orden_de_llegada() {
        Tournament torneo = conEquipos("Rojos", "Azules", "Verdes", "Grises");

        BracketRound ronda = torneo.startFirstRound();

        assertThat(ronda.pairings()).hasSize(2);
        assertThat(ronda.pairings()).noneMatch(Pairing::isBye);
    }

    @Test
    void con_un_numero_impar_alguien_pasa_directo() {
        Tournament torneo = conEquipos("Rojos", "Azules", "Verdes");

        BracketRound ronda = torneo.startFirstRound();

        assertThat(ronda.pairings()).hasSize(2);
        assertThat(ronda.pairings().getLast().isBye()).isTrue();
        assertThat(ronda.pairings().getLast().winner()).isPresent();
    }

    @Test
    void el_pase_directo_no_le_toca_dos_veces_al_mismo() {
        Tournament torneo = conEquipos("Rojos", "Azules", "Verdes", "Grises", "Lilas");
        BracketRound primera = torneo.startFirstRound();
        PlayerId primerAgraciado = primera.pairings().getLast().home();
        primera.pairings().stream().filter(cruce -> !cruce.isBye())
                .forEach(cruce -> cruce.resolvedBy(cruce.home()));

        BracketRound segunda = torneo.startNextRound();

        assertThat(segunda.pairings().getLast().isBye()).isTrue();
        assertThat(segunda.pairings().getLast().home()).isNotEqualTo(primerAgraciado);
    }

    @Test
    void no_se_puede_avanzar_con_mesas_sin_terminar() {
        Tournament torneo = conEquipos("Rojos", "Azules", "Verdes", "Grises");
        torneo.startFirstRound();

        assertThatThrownBy(torneo::startNextRound).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nadie_se_une_con_el_torneo_ya_empezado() {
        Tournament torneo = conEquipos("Rojos", "Azules");
        torneo.startFirstRound();

        assertThatThrownBy(() -> torneo.join("Tarde"))
                .isInstanceOf(com.cyberdeck.domain.exception.GameNotJoinableException.class);
    }

    @Test
    void con_un_solo_ganador_el_torneo_tiene_campeon() {
        Tournament torneo = conEquipos("Rojos", "Azules");
        BracketRound ronda = torneo.startFirstRound();
        List<PlayerId> equipos = torneo.teams().stream().map(TournamentTeam::id).toList();

        ronda.pairings().getFirst().resolvedBy(equipos.getFirst());

        assertThat(torneo.isOver()).isTrue();
        assertThat(torneo.champion()).map(TournamentTeam::displayName).contains("Rojos");
    }

    @Test
    void hacen_falta_al_menos_dos_equipos() {
        Tournament torneo = conEquipos("Solos");

        assertThatThrownBy(torneo::startFirstRound).isInstanceOf(IllegalStateException.class);
    }
}
