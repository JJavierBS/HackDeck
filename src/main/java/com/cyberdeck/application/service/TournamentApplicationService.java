package com.cyberdeck.application.service;

import com.cyberdeck.application.exception.AccessDeniedException;
import com.cyberdeck.application.port.in.TeamPlacement;
import com.cyberdeck.application.port.in.TournamentAccess;
import com.cyberdeck.application.port.in.TournamentUseCase;
import com.cyberdeck.application.port.out.AccessTokenPort;
import com.cyberdeck.application.port.out.GameRepository;
import com.cyberdeck.application.port.out.GameStateBroadcaster;
import com.cyberdeck.application.port.out.TournamentRepository;
import com.cyberdeck.domain.exception.GameNotFoundException;
import com.cyberdeck.domain.model.BracketRound;
import com.cyberdeck.domain.model.Game;
import com.cyberdeck.domain.model.GameSettings;
import com.cyberdeck.domain.model.JoinCode;
import com.cyberdeck.domain.model.Pairing;
import com.cyberdeck.domain.model.Participant;
import com.cyberdeck.domain.model.ParticipantSession;
import com.cyberdeck.domain.model.PlayerId;
import com.cyberdeck.domain.model.TeamId;
import com.cyberdeck.domain.model.Tournament;
import com.cyberdeck.domain.model.TournamentId;
import com.cyberdeck.domain.model.TournamentSession;
import com.cyberdeck.domain.model.TournamentTeam;
import org.springframework.stereotype.Service;

import com.cyberdeck.application.view.TournamentView;
import com.cyberdeck.domain.model.CiaPillar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public final class TournamentApplicationService implements TournamentUseCase {

    private static final int MAX_JOIN_CODE_ATTEMPTS = 100;

    private final TournamentRepository tournaments;
    private final GameRepository games;
    private final GameStateBroadcaster broadcaster;
    private final AccessTokenPort tokens;

    public TournamentApplicationService(
            TournamentRepository tournaments,
            GameRepository games,
            GameStateBroadcaster broadcaster,
            AccessTokenPort tokens) {
        this.tournaments = Objects.requireNonNull(tournaments, "tournaments");
        this.games = Objects.requireNonNull(games, "games");
        this.broadcaster = Objects.requireNonNull(broadcaster, "broadcaster");
        this.tokens = Objects.requireNonNull(tokens, "tokens");
    }

    @Override
    public TournamentAccess createTournament(GameSettings settings) {
        Participant instructor = Participant.instructor();
        Tournament torneo = Tournament.create(uniqueJoinCode(), instructor, settings);
        tournaments.save(torneo);
        return new TournamentAccess(
                torneo.id(),
                torneo.joinCode(),
                instructor.displayName(),
                tokens.issue(new ParticipantSession(instructorGameId(torneo), instructor)));
    }

    @Override
    public Optional<TournamentAccess> joinTournament(JoinCode joinCode, String displayName) {
        return tournaments.findByJoinCode(joinCode).map(torneo -> {
            TournamentTeam equipo = torneo.join(displayName);
            tournaments.save(torneo);
            return new TournamentAccess(
                    torneo.id(),
                    torneo.joinCode(),
                    equipo.displayName(),
                    tokens.issueTournament(
                            new TournamentSession(torneo.id(), equipo.id(), equipo.displayName())));
        });
    }

    @Override
    public void startTournament(TournamentId id, ParticipantSession session) {
        Tournament torneo = requireTournament(id);
        requireInstructor(torneo, session);
        sembrarMesas(torneo, torneo.startFirstRound());
        tournaments.save(torneo);
    }

    @Override
    public void startNextRound(TournamentId id, ParticipantSession session) {
        Tournament torneo = requireTournament(id);
        requireInstructor(torneo, session);
        cerrarRondaEnCurso(torneo);
        sembrarMesas(torneo, torneo.startNextRound());
        tournaments.save(torneo);
    }

    @Override
    public TeamPlacement placementOf(TournamentSession session) {
        Tournament torneo = requireTournament(session.tournamentId());
        int ronda = torneo.currentRound().map(BracketRound::number).orElse(0);
        cerrarRondaEnCurso(torneo);

        Optional<Pairing> cruce = torneo.pairingOf(session.teamId());
        if (cruce.isEmpty()) {
            return TeamPlacement.out(torneo.phase().name().equals("LOBBY") ? "WAITING" : "ELIMINATED", ronda);
        }
        Pairing emparejamiento = cruce.get();
        if (emparejamiento.isBye()) {
            return TeamPlacement.waiting(ronda);
        }
        Optional<Game> mesa = emparejamiento.gameId().flatMap(games::findById);
        if (mesa.isEmpty()) {
            return TeamPlacement.waiting(ronda);
        }
        Game partida = mesa.get();
        Participant participante = partida.players().values().stream()
                .filter(jugador -> jugador.id().equals(session.teamId()))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Ese equipo no esta sentado en la mesa"));
        return TeamPlacement.playing(
                partida.id().toString(),
                tokens.issue(new ParticipantSession(partida.id(), participante)),
                participante.team().name(),
                ronda);
    }

    @Override
    public TournamentView view(TournamentId id, ParticipantSession session) {
        Tournament torneo = requireTournament(id);
        requireInstructor(torneo, session);
        cerrarRondaEnCurso(torneo);

        BracketRound ronda = torneo.currentRound().orElse(null);
        List<TournamentView.TableView> mesas = ronda == null
                ? List.of()
                : ronda.pairings().stream().map(cruce -> mesaDe(torneo, cruce)).toList();

        return new TournamentView(
                torneo.id().toString(),
                torneo.joinCode().toString(),
                torneo.phase().name(),
                ronda == null ? 0 : ronda.number(),
                ronda != null && ronda.isComplete(),
                torneo.champion().map(TournamentTeam::displayName).orElse(null),
                clasificacion(torneo),
                mesas);
    }

    private TournamentView.TableView mesaDe(Tournament torneo, Pairing cruce) {
        String local = nombreDe(torneo, cruce.home());
        String visitante = cruce.away().map(equipo -> nombreDe(torneo, equipo)).orElse(null);
        Game mesa = cruce.gameId().flatMap(games::findById).orElse(null);
        return new TournamentView.TableView(
                mesa == null ? null : mesa.id().toString(),
                mesa == null ? null : tokens.issue(new ParticipantSession(mesa.id(), torneo.instructor())),
                local,
                visitante,
                mesa == null ? "BYE" : mesa.phase().name(),
                mesa == null || mesa.halves().isEmpty() ? null : mesa.currentHalf().number(),
                mesa == null || mesa.halves().isEmpty() ? 0 : mesa.currentHalf().currentRound().number(),
                mesa == null || mesa.halves().isEmpty() ? Map.of() : triadaDe(mesa),
                mesa == null || mesa.halves().isEmpty()
                        ? List.of()
                        : mesa.currentHalf().currentRound().readyTeams().stream().map(TeamId::name).sorted().toList(),
                cruce.winner().map(equipo -> nombreDe(torneo, equipo)).orElse(null));
    }

    private static Map<String, Integer> triadaDe(Game mesa) {
        Map<String, Integer> triada = new LinkedHashMap<>();
        for (CiaPillar pilar : CiaPillar.values()) {
            triada.put(pilar.name(), mesa.currentHalf().ciaState().levelOf(pilar));
        }
        return triada;
    }

    private List<TournamentView.TeamStanding> clasificacion(Tournament torneo) {
        List<TournamentView.TeamStanding> tabla = new ArrayList<>();
        for (TournamentTeam equipo : torneo.teams()) {
            int victorias = 0;
            int defendido = 0;
            for (BracketRound ronda : torneo.rounds()) {
                for (Pairing cruce : ronda.pairings()) {
                    if (!cruce.involves(equipo.id())) {
                        continue;
                    }
                    // Un pase directo no es una victoria: quien no juega no gana.
                    if (!cruce.isBye() && cruce.winner().map(equipo.id()::equals).orElse(false)) {
                        victorias++;
                    }
                    defendido += cruce.gameId().flatMap(games::findById)
                            .filter(partida -> partida.result() != null)
                            .map(partida -> partida.result().defendedCia().getOrDefault(
                                    bandoDe(partida, equipo.id()), 0))
                            .orElse(0);
                }
            }
            tabla.add(new TournamentView.TeamStanding(
                    equipo.id().toString(), equipo.displayName(), estadoDe(torneo, equipo), victorias, defendido));
        }
        tabla.sort(Comparator.comparingInt(TournamentView.TeamStanding::wins).reversed()
                .thenComparing(Comparator.comparingInt(TournamentView.TeamStanding::defendedCia).reversed()));
        return tabla;
    }

    private static TeamId bandoDe(Game partida, PlayerId equipo) {
        return partida.players().entrySet().stream()
                .filter(entrada -> entrada.getValue().id().equals(equipo))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(TeamId.A);
    }

    private String estadoDe(Tournament torneo, TournamentTeam equipo) {
        if (torneo.champion().map(campeon -> campeon.id().equals(equipo.id())).orElse(false)) {
            return "CHAMPION";
        }
        return torneo.pairingOf(equipo.id()).isPresent() || torneo.phase().name().equals("LOBBY")
                ? "IN"
                : "ELIMINATED";
    }

    private String nombreDe(Tournament torneo, PlayerId equipo) {
        return torneo.team(equipo).map(TournamentTeam::displayName).orElse("?");
    }

    /**
     * Una mesa terminada decide su cruce. Se hace al consultar en vez de con
     * un aviso desde la partida para que el torneo no tenga que enterarse de
     * cada ronda que se resuelve en cada mesa.
     */
    private void cerrarRondaEnCurso(Tournament torneo) {
        torneo.currentRound().ifPresent(ronda -> ronda.pairings().stream()
                .filter(pairing -> pairing.winner().isEmpty())
                .forEach(pairing -> pairing.gameId()
                        .flatMap(games::findById)
                        .filter(Game::isOver)
                        .ifPresent(partida -> pairing.resolvedBy(ganadorDe(partida, pairing)))));
        tournaments.save(torneo);
    }

    /**
     * En un bracket alguien tiene que pasar: si el match acaba en empate lo
     * hace quien mejor defendio, y si tampoco eso los separa, el local.
     */
    private PlayerId ganadorDe(Game partida, Pairing cruce) {
        TeamId ganador = partida.result() == null ? null : partida.result().winner();
        if (ganador == null) {
            int defendidoPorA = partida.result() == null ? 0 : partida.result().defendedCia().getOrDefault(TeamId.A, 0);
            int defendidoPorB = partida.result() == null ? 0 : partida.result().defendedCia().getOrDefault(TeamId.B, 0);
            ganador = defendidoPorB > defendidoPorA ? TeamId.B : TeamId.A;
        }
        return partida.playerOf(ganador).map(Participant::id).orElse(cruce.home());
    }

    private void sembrarMesas(Tournament torneo, BracketRound ronda) {
        for (Pairing cruce : ronda.pairings()) {
            if (cruce.isBye()) {
                continue;
            }
            Game mesa = Game.create(uniqueGameCode(), torneo.instructor(), torneo.settings());
            mesa.join(jugador(torneo, cruce.home(), TeamId.A));
            mesa.join(jugador(torneo, cruce.away().orElseThrow(), TeamId.B));
            mesa.startMatch();
            // En torneo el ritmo lo lleva el reloj: una persona sola no puede
            // estar pendiente de resolver seis mesas a mano.
            mesa.setAutoResolve(true);
            games.save(mesa);
            cruce.playedIn(mesa.id());
            broadcaster.broadcastState(mesa.id(), mesa);
        }
    }

    private Participant jugador(Tournament torneo, PlayerId equipo, TeamId bando) {
        TournamentTeam datos = torneo.team(equipo).orElseThrow();
        return new Participant(datos.id(), com.cyberdeck.domain.model.ParticipantKind.PLAYER, bando, datos.displayName());
    }

    private Tournament requireTournament(TournamentId id) {
        return tournaments.findById(id).orElseThrow(() -> new GameNotFoundException(id.toString()));
    }

    private void requireInstructor(Tournament torneo, ParticipantSession session) {
        if (!session.isInstructor() || !session.participant().id().equals(torneo.instructor().id())) {
            throw new AccessDeniedException("Solo el instructor del torneo puede hacer esto");
        }
    }

    /** El instructor de un torneo lleva un token con el id del torneo por partida. */
    private com.cyberdeck.domain.model.GameId instructorGameId(Tournament torneo) {
        return new com.cyberdeck.domain.model.GameId(torneo.id().value());
    }

    private JoinCode uniqueJoinCode() {
        for (int intento = 0; intento < MAX_JOIN_CODE_ATTEMPTS; intento++) {
            JoinCode candidato = JoinCode.generate();
            if (tournaments.findByJoinCode(candidato).isEmpty() && !games.existsByJoinCode(candidato)) {
                return candidato;
            }
        }
        throw new IllegalStateException("No se ha podido generar un codigo de torneo libre");
    }

    private JoinCode uniqueGameCode() {
        for (int intento = 0; intento < MAX_JOIN_CODE_ATTEMPTS; intento++) {
            JoinCode candidato = JoinCode.generate();
            if (!games.existsByJoinCode(candidato) && tournaments.findByJoinCode(candidato).isEmpty()) {
                return candidato;
            }
        }
        throw new IllegalStateException("No se ha podido generar un codigo de mesa libre");
    }
}
