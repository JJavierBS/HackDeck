package com.hackdeck.application.service;

import com.hackdeck.apoyo.AzarControlado;
import com.hackdeck.apoyo.Dobles;
import com.hackdeck.apoyo.Partidas;
import com.hackdeck.application.exception.CapacityExceededException;
import com.hackdeck.application.port.in.TeamPlacement;
import com.hackdeck.application.port.in.TournamentAccess;
import com.hackdeck.domain.model.Game;
import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.ParticipantSession;
import com.hackdeck.domain.model.Tournament;
import com.hackdeck.domain.model.TournamentPhase;
import com.hackdeck.domain.model.TournamentSession;
import com.hackdeck.domain.rules.DefaultRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Lo que el equipo ve entre mesa y mesa: es su unica ventana al bracket.
 */
class TorneoTest {

    private Dobles.Repositorio partidas;
    private Dobles.RepositorioTorneos torneos;
    private TournamentApplicationService servicio;

    @BeforeEach
    void preparar() {
        partidas = new Dobles.Repositorio();
        torneos = new Dobles.RepositorioTorneos();
        servicio = new TournamentApplicationService(
                torneos,
                partidas,
                new Dobles.Difusor(),
                new Dobles.Tokens(),
                new DefaultRuleEngine(Partidas.catalogoVacio(), AzarControlado.siempre()),
                Partidas.RETENCION,
                Partidas.cupoDeTorneos());
    }

    @Test
    void el_que_pasa_directo_lo_sabe() {
        TournamentAccess torneo = conEquipos(3);
        servicio.startTournament(torneo.tournamentId(), instructor(torneo));

        List<TeamPlacement> sitios = sitios(torneo);

        assertThat(sitios).extracting(TeamPlacement::status).containsExactlyInAnyOrder("PLAYING", "PLAYING", "BYE");
    }

    @Test
    void al_acabar_la_mesa_nadie_vuelve_a_entrar_en_ella() {
        TournamentAccess torneo = conEquipos(3);
        servicio.startTournament(torneo.tournamentId(), instructor(torneo));
        terminarMesas();

        List<TeamPlacement> sitios = sitios(torneo);

        // El ganador espera la ronda siguiente y el perdedor ya sabe que esta fuera.
        assertThat(sitios).extracting(TeamPlacement::status).containsExactlyInAnyOrder("WAITING", "ELIMINATED", "BYE");
        assertThat(sitios).extracting(TeamPlacement::gameId).containsOnlyNulls();
    }

    @Test
    void el_torneo_se_da_por_acabado_en_cuanto_hay_campeon() {
        TournamentAccess torneo = conEquipos(2);
        servicio.startTournament(torneo.tournamentId(), instructor(torneo));
        terminarMesas();

        assertThat(sitios(torneo)).extracting(TeamPlacement::status)
                .containsExactlyInAnyOrder("CHAMPION", "ELIMINATED");
        assertThat(servicio.view(torneo.tournamentId(), instructor(torneo)).phase()).isEqualTo("FINISHED");
        assertThat(servicio.view(torneo.tournamentId(), instructor(torneo)).standings())
                .extracting(fila -> fila.status())
                .containsExactlyInAnyOrder("CHAMPION", "ELIMINATED");
    }

    @Test
    void sin_sitio_para_todas_las_mesas_el_torneo_no_arranca_a_medias() {
        TournamentApplicationService apretado = new TournamentApplicationService(
                torneos,
                partidas,
                new Dobles.Difusor(),
                new Dobles.Tokens(),
                new DefaultRuleEngine(Partidas.catalogoVacio(), AzarControlado.siempre()),
                Partidas.retencionCon(1),
                Partidas.cupoDeTorneos());
        TournamentAccess torneo = apretado.createTournament(Partidas.AJUSTES, "test");
        for (int equipo = 1; equipo <= 4; equipo++) {
            apretado.joinTournament(torneo.joinCode(), "Equipo " + equipo);
        }

        assertThatThrownBy(() -> apretado.startTournament(torneo.tournamentId(), instructor(torneo)))
                .isInstanceOf(CapacityExceededException.class);

        // Ni una mesa sembrada ni el bracket tocado: o entran las dos, o ninguna.
        assertThat(partidas.findAll()).isEmpty();
        assertThat(torneos.findById(torneo.tournamentId()).orElseThrow().phase())
                .isEqualTo(TournamentPhase.LOBBY);
    }

    private TournamentAccess conEquipos(int cuantos) {
        TournamentAccess torneo = servicio.createTournament(Partidas.AJUSTES, "test");
        for (int equipo = 1; equipo <= cuantos; equipo++) {
            servicio.joinTournament(torneo.joinCode(), "Equipo " + equipo);
        }
        return torneo;
    }

    private void terminarMesas() {
        partidas.findInProgress().forEach(Game::closeMatch);
    }

    private List<TeamPlacement> sitios(TournamentAccess torneo) {
        Tournament aggregate = torneos.findById(torneo.tournamentId()).orElseThrow();
        return aggregate.teams().stream()
                .map(equipo -> servicio.placementOf(
                        new TournamentSession(aggregate.id(), equipo.id(), equipo.displayName())))
                .toList();
    }

    private ParticipantSession instructor(TournamentAccess torneo) {
        Tournament aggregate = torneos.findById(torneo.tournamentId()).orElseThrow();
        return new ParticipantSession(new GameId(aggregate.id().value()), aggregate.instructor());
    }
}
