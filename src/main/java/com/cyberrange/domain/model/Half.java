package com.cyberrange.domain.model;

import com.cyberrange.domain.exception.InsufficientBudgetException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Una mitad del match. Cada mitad arranca limpia (triada a 100 y
 * presupuestos al valor inicial) y con los bandos cambiados respecto a la
 * anterior, para que las dos sean comparables al puntuar.
 */
public final class Half {

    public static final int FIRST = 1;
    public static final int SECOND = 2;

    private final int number;
    private final TeamId attackingTeam;
    private final GameSettings settings;
    private final List<Round> rounds = new ArrayList<>();
    private final Map<TeamId, Integer> budgets = new EnumMap<>(TeamId.class);
    private CiaState ciaState = CiaState.intact();
    private Integer takedownRound;
    private boolean finished;

    public Half(int number, TeamId attackingTeam, GameSettings settings) {
        this.number = number;
        this.attackingTeam = Objects.requireNonNull(attackingTeam, "attackingTeam");
        this.settings = Objects.requireNonNull(settings, "settings");
        for (TeamId team : TeamId.values()) {
            budgets.put(team, settings.initialBudget());
        }
        rounds.add(new Round(1));
    }

    public int number() {
        return number;
    }

    public TeamId attackingTeam() {
        return attackingTeam;
    }

    public TeamId defendingTeam() {
        return attackingTeam == TeamId.A ? TeamId.B : TeamId.A;
    }

    public CiaState ciaState() {
        return ciaState;
    }

    public List<Round> rounds() {
        return List.copyOf(rounds);
    }

    public Round currentRound() {
        return rounds.getLast();
    }

    public Map<TeamId, Integer> budgets() {
        return Collections.unmodifiableMap(budgets);
    }

    public int budgetOf(TeamId team) {
        return budgets.getOrDefault(team, 0);
    }

    /**
     * Ronda en la que cayo un pilar, o vacio si la mitad aguanto entera.
     * Es el criterio de desempate cuando ambos equipos consiguen derribo.
     */
    public Integer takedownRound() {
        return takedownRound;
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * Suma de los tres pilares tal como quedaron: lo que defendio el equipo
     * defensor de esta mitad.
     */
    public int defendedCia() {
        int total = 0;
        for (CiaPillar pillar : CiaPillar.values()) {
            total += ciaState.levelOf(pillar);
        }
        return total;
    }

    public void spend(TeamId team, int cost) {
        int available = budgetOf(team);
        if (cost > available) {
            throw new InsufficientBudgetException(available, cost);
        }
        budgets.put(team, available - cost);
    }

    public void applyResolvedState(CiaState resolvedState) {
        this.ciaState = Objects.requireNonNull(resolvedState, "resolvedState");
    }

    public void recordTakedown() {
        this.takedownRound = currentRound().number();
    }

    public boolean isLastRound() {
        return currentRound().number() >= settings.roundsPerHalf();
    }

    public void advanceRound() {
        rounds.add(new Round(currentRound().number() + 1));
        // El presupuesto no gastado se acumula: ahorrar para una accion cara
        // es una decision valida.
        budgets.replaceAll((team, budget) -> budget + settings.incomePerRound());
    }

    public void finish() {
        this.finished = true;
    }
}
