package com.cyberrange.domain.model;

import com.cyberrange.domain.exception.GameNotJoinableException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Raiz de agregado: un match completo, sus dos mitades y quien lo juega.
 * El servidor es la unica fuente de verdad del estado del juego.
 */
public final class Game {

    private final GameId id;
    private final JoinCode joinCode;
    private final Participant instructor;
    private final GameSettings settings;
    private final Map<TeamId, Participant> players = new EnumMap<>(TeamId.class);
    private final List<Half> halves = new ArrayList<>();
    private GamePhase phase;
    private MatchResult result;

    public Game(GameId id, JoinCode joinCode, Participant instructor, GameSettings settings, GamePhase phase) {
        this.id = Objects.requireNonNull(id, "id");
        this.joinCode = Objects.requireNonNull(joinCode, "joinCode");
        this.instructor = Objects.requireNonNull(instructor, "instructor");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.phase = Objects.requireNonNull(phase, "phase");
    }

    public static Game create(JoinCode joinCode, Participant instructor, GameSettings settings) {
        return new Game(GameId.newId(), joinCode, instructor, settings, GamePhase.PREPARATION);
    }

    public GameId id() {
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

    public Map<TeamId, Participant> players() {
        return Collections.unmodifiableMap(players);
    }

    public GamePhase phase() {
        return phase;
    }

    public MatchResult result() {
        return result;
    }

    public List<Half> halves() {
        return List.copyOf(halves);
    }

    public Half currentHalf() {
        if (halves.isEmpty()) {
            throw new IllegalStateException("La partida todavia no ha empezado");
        }
        return halves.getLast();
    }

    public CiaState ciaState() {
        return currentHalf().ciaState();
    }

    public Round currentRound() {
        return currentHalf().currentRound();
    }

    public List<Round> rounds() {
        return currentHalf().rounds();
    }

    /**
     * Bando que juega un equipo en la mitad en curso. Cambia al pasar a la
     * segunda mitad.
     */
    public Role sideOf(TeamId team) {
        return team == currentHalf().attackingTeam() ? Role.ATTACKER : Role.DEFENDER;
    }

    public Optional<Participant> playerOf(TeamId team) {
        return Optional.ofNullable(players.get(team));
    }

    public Optional<TeamId> firstFreeTeam() {
        for (TeamId team : TeamId.values()) {
            if (!players.containsKey(team)) {
                return Optional.of(team);
            }
        }
        return Optional.empty();
    }

    public void join(Participant player) {
        if (phase != GamePhase.PREPARATION) {
            throw new GameNotJoinableException("La partida ya ha empezado");
        }
        if (players.containsKey(player.team())) {
            throw new GameNotJoinableException("El equipo " + player.team() + " ya esta ocupado");
        }
        players.put(player.team(), player);
    }

    /**
     * Cierra el lobby y arranca la primera mitad, con el equipo A atacando.
     */
    public void startMatch() {
        if (phase != GamePhase.PREPARATION) {
            throw new IllegalStateException("La partida ya ha salido de la fase de preparacion");
        }
        if (players.size() < TeamId.values().length) {
            throw new IllegalStateException("Faltan equipos por unirse a la partida");
        }
        phase = GamePhase.IN_PROGRESS;
        halves.add(new Half(Half.FIRST, TeamId.A, settings));
    }

    public void enqueue(ActionIntent action, int cost) {
        requireInProgress();
        Half half = currentHalf();
        half.spend(teamPlaying(action.team()), cost);
        half.currentRound().enqueue(action);
    }

    /**
     * Aplica lo que decidio el motor de reglas y hace avanzar la maquina de
     * estados: derribar un pilar cierra la mitad en el acto, y agotar las
     * rondas tambien. Cerrar la primera mitad arranca la segunda con los
     * bandos cambiados; cerrar la segunda termina el match.
     */
    public void applyRoundResolution(CiaState resolvedState, List<GameEvent> events, boolean takedown) {
        requireInProgress();
        Half half = currentHalf();
        half.applyResolvedState(resolvedState);
        events.forEach(half.currentRound()::recordEvent);

        if (takedown) {
            half.recordTakedown();
            endHalf();
        } else if (half.isLastRound()) {
            endHalf();
        } else {
            half.advanceRound();
        }
    }

    public void recordResult(MatchResult matchResult) {
        this.result = Objects.requireNonNull(matchResult, "matchResult");
    }

    public void requireInProgress() {
        if (phase != GamePhase.IN_PROGRESS) {
            throw new IllegalStateException("La partida no esta en curso");
        }
    }

    public boolean isOver() {
        return phase == GamePhase.FINISHED;
    }

    private void endHalf() {
        Half half = currentHalf();
        half.finish();
        if (half.number() == Half.FIRST) {
            halves.add(new Half(Half.SECOND, half.defendingTeam(), settings));
        } else {
            phase = GamePhase.FINISHED;
        }
    }

    private TeamId teamPlaying(Role side) {
        Half half = currentHalf();
        return side == Role.ATTACKER ? half.attackingTeam() : half.defendingTeam();
    }
}
