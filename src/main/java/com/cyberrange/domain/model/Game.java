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
 * Raiz de agregado: una partida completa (fase, triada CIA y rondas).
 * El servidor es la unica fuente de verdad del estado del juego.
 */
public final class Game {

    private final GameId id;
    private final JoinCode joinCode;
    private final Participant instructor;
    private final Map<TeamId, Participant> players = new EnumMap<>(TeamId.class);
    private GamePhase phase;
    private CiaState ciaState;
    private final List<Round> rounds = new ArrayList<>();

    /**
     * Equipo que ataca en la mitad en curso. Fijo mientras no exista el
     * modelo de mitades: al implementarlo, cambiar al pasar a la mitad 2.
     */
    // TODO: Fase 1 del roadmap, cambio de bando entre mitades.
    private TeamId attackingTeam = TeamId.A;

    public Game(GameId id, JoinCode joinCode, Participant instructor, GamePhase phase, CiaState ciaState) {
        this.id = Objects.requireNonNull(id, "id");
        this.joinCode = Objects.requireNonNull(joinCode, "joinCode");
        this.instructor = Objects.requireNonNull(instructor, "instructor");
        this.phase = Objects.requireNonNull(phase, "phase");
        this.ciaState = Objects.requireNonNull(ciaState, "ciaState");
    }

    public static Game create(JoinCode joinCode, Participant instructor) {
        return new Game(GameId.newId(), joinCode, instructor, GamePhase.PREPARATION, CiaState.intact());
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

    public Map<TeamId, Participant> players() {
        return Collections.unmodifiableMap(players);
    }

    public GamePhase phase() {
        return phase;
    }

    public CiaState ciaState() {
        return ciaState;
    }

    public TeamId attackingTeam() {
        return attackingTeam;
    }

    public List<Round> rounds() {
        return List.copyOf(rounds);
    }

    /**
     * Bando que juega un equipo en la mitad en curso.
     */
    public Role sideOf(TeamId team) {
        return team == attackingTeam ? Role.ATTACKER : Role.DEFENDER;
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

    public Round currentRound() {
        if (rounds.isEmpty()) {
            throw new IllegalStateException("La partida todavia no tiene ninguna ronda");
        }
        return rounds.getLast();
    }

    public void enqueue(ActionIntent action) {
        requireInProgress();
        currentRound().enqueue(action);
    }

    public void requireInProgress() {
        if (phase != GamePhase.IN_PROGRESS) {
            throw new IllegalStateException("La partida no esta en curso");
        }
    }

    /**
     * Cierra la fase de preparacion y arranca la primera ronda.
     */
    public void beginFirstRound() {
        if (phase != GamePhase.PREPARATION) {
            throw new IllegalStateException("La partida ya ha salido de la fase de preparacion");
        }
        phase = GamePhase.IN_PROGRESS;
        rounds.add(new Round(1));
    }

    /**
     * Avanza a la siguiente ronda tras resolver la actual.
     */
    public void advanceRound() {
        requireInProgress();
        rounds.add(new Round(currentRound().number() + 1));
    }

    public void applyResolvedState(CiaState resolvedState) {
        this.ciaState = Objects.requireNonNull(resolvedState, "resolvedState");
    }

    public void finish() {
        phase = GamePhase.FINISHED;
    }

    public boolean isOver() {
        return phase == GamePhase.FINISHED;
    }
}
