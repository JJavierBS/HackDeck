package com.hackdeck.domain.model;

import com.hackdeck.domain.exception.GameNotJoinableException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Varias mesas gobernadas a la vez. Los equipos solo teclean un codigo: el
 * torneo los va sentando y los mueve de mesa segun avanza el bracket, sin
 * que ellos tengan que hacer nada.
 */
public final class Tournament {

    /**
     * Tope de equipos por torneo. Con el codigo proyectado en la pared se une
     * quien quiera, asi que sin limite una sola persona podia apuntar miles de
     * equipos y, al empezar, obligar al servidor a crear miles de mesas.
     * Sesenta y cuatro son cinco rondas de bracket: mas no cabe en una clase.
     */
    public static final int MAX_TEAMS = 64;

    private final TournamentId id;
    private final JoinCode joinCode;
    private final Participant instructor;
    private final GameSettings settings;
    private final Map<PlayerId, TournamentTeam> teams = new LinkedHashMap<>();
    private final List<BracketRound> rounds = new ArrayList<>();
    private TournamentPhase phase = TournamentPhase.LOBBY;
    private Instant lastActivityAt = Instant.now();

    public Tournament(TournamentId id, JoinCode joinCode, Participant instructor, GameSettings settings) {
        this.id = Objects.requireNonNull(id, "id");
        this.joinCode = Objects.requireNonNull(joinCode, "joinCode");
        this.instructor = Objects.requireNonNull(instructor, "instructor");
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public static Tournament create(JoinCode joinCode, Participant instructor, GameSettings settings) {
        return new Tournament(TournamentId.newId(), joinCode, instructor, settings);
    }

    public TournamentId id() {
        return id;
    }

    public JoinCode joinCode() {
        return joinCode;
    }

    public Participant instructor() {
        return instructor;
    }

    public GameSettings settings() {
        return settings;
    }

    public TournamentPhase phase() {
        return phase;
    }

    public List<TournamentTeam> teams() {
        return List.copyOf(teams.values());
    }

    public Optional<TournamentTeam> team(PlayerId id) {
        return Optional.ofNullable(teams.get(id));
    }

    public List<BracketRound> rounds() {
        return List.copyOf(rounds);
    }

    public Optional<BracketRound> currentRound() {
        return rounds.isEmpty() ? Optional.empty() : Optional.of(rounds.getLast());
    }

    public TournamentTeam join(String displayName) {
        if (phase != TournamentPhase.LOBBY) {
            throw new GameNotJoinableException("El torneo ya ha empezado");
        }
        if (teams.size() >= MAX_TEAMS) {
            throw new GameNotJoinableException("El torneo ya tiene el maximo de " + MAX_TEAMS + " equipos");
        }
        TournamentTeam equipo = new TournamentTeam(PlayerId.newId(), Participant.player(TeamId.A, displayName).displayName());
        teams.put(equipo.id(), equipo);
        touch();
        return equipo;
    }

    public Instant lastActivityAt() {
        return lastActivityAt;
    }

    private void touch() {
        lastActivityAt = Instant.now();
    }

    /**
     * Empareja por orden de llegada. Con un numero impar de equipos, el
     * ultimo pasa directo a la ronda siguiente.
     */
    /**
     * Mesas que hara falta sembrar en la proxima ronda. Es consulta pura: se
     * pregunta antes de arrancar, porque quedarse sin sitio a mitad del
     * sembrado dejaria el bracket con unos cruces jugandose y otros no.
     * Quien pasa directo no ocupa mesa.
     */
    public int tablesForNextRound() {
        if (phase == TournamentPhase.LOBBY) {
            return teams.size() / 2;
        }
        return currentRound().map(ronda -> ronda.winners().size() / 2).orElse(0);
    }

    public BracketRound startFirstRound() {
        if (phase != TournamentPhase.LOBBY) {
            throw new IllegalStateException("El torneo ya ha empezado");
        }
        if (teams.size() < 2) {
            throw new IllegalStateException("Hacen falta al menos dos equipos");
        }
        phase = TournamentPhase.IN_PROGRESS;
        return addRound(List.copyOf(teams.keySet()));
    }

    public BracketRound startNextRound() {
        BracketRound actual = currentRound().orElseThrow(() -> new IllegalStateException("El torneo no ha empezado"));
        if (!actual.isComplete()) {
            throw new IllegalStateException("Todavia hay mesas sin terminar");
        }
        List<PlayerId> ganadores = actual.winners();
        if (ganadores.size() < 2) {
            throw new IllegalStateException("El torneo ya tiene ganador");
        }
        return addRound(ganadores);
    }

    public boolean isOver() {
        return phase == TournamentPhase.FINISHED
                || currentRound().map(ronda -> ronda.isComplete() && ronda.winners().size() == 1).orElse(false);
    }

    public Optional<TournamentTeam> champion() {
        if (!isOver()) {
            return Optional.empty();
        }
        return currentRound().flatMap(ronda -> ronda.winners().stream().findFirst()).flatMap(this::team);
    }

    public void finish() {
        phase = TournamentPhase.FINISHED;
        touch();
    }

    public Optional<Pairing> pairingOf(PlayerId team) {
        return currentRound().flatMap(ronda -> ronda.pairings().stream()
                .filter(pairing -> pairing.involves(team))
                .findFirst());
    }

    public Optional<Pairing> pairingOfGame(GameId gameId) {
        return currentRound().flatMap(ronda -> ronda.pairings().stream()
                .filter(pairing -> pairing.gameId().map(gameId::equals).orElse(false))
                .findFirst());
    }

    private BracketRound addRound(List<PlayerId> participantes) {
        List<PlayerId> orden = new ArrayList<>(participantes);
        if (orden.size() % 2 == 1) {
            // El pase directo va al final, y se lo lleva alguien que no lo
            // haya tenido ya: si no, con cinco equipos el mismo se planta en
            // la final sin jugar una sola partida.
            orden.stream()
                    .filter(equipo -> !hasHadBye(equipo))
                    .findFirst()
                    .ifPresent(elegido -> {
                        orden.remove(elegido);
                        orden.add(elegido);
                    });
        }
        List<Pairing> cruces = new ArrayList<>();
        for (int i = 0; i < orden.size(); i += 2) {
            PlayerId local = orden.get(i);
            PlayerId visitante = i + 1 < orden.size() ? orden.get(i + 1) : null;
            cruces.add(new Pairing(local, visitante));
        }
        BracketRound ronda = new BracketRound(rounds.size() + 1, cruces);
        rounds.add(ronda);
        touch();
        return ronda;
    }

    public boolean hasHadBye(PlayerId team) {
        return rounds.stream()
                .flatMap(ronda -> ronda.pairings().stream())
                .anyMatch(cruce -> cruce.isBye() && cruce.home().equals(team));
    }
}
