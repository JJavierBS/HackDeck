package com.hackdeck.domain.model;

import com.hackdeck.domain.catalog.KillChainPhase;
import com.hackdeck.domain.exception.InsufficientBudgetException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Arranca limpia (triada a 100 y presupuestos al inicial) y con los bandos
 * cambiados respecto a la anterior, para que las dos sean comparables.
 */
public final class Half {

    public static final int FIRST = 1;
    public static final int SECOND = 2;

    private final int number;
    private final TeamId attackingTeam;
    private final GameSettings settings;
    private final List<Round> rounds = new ArrayList<>();
    private final Map<TeamId, Integer> budgets = new EnumMap<>(TeamId.class);
    private final List<ActiveCard> activeCards = new ArrayList<>();
    /** El atacante empieza pudiendo hacer reconocimiento y nada mas. */
    private final EnumSet<KillChainPhase> unlockedPhases = EnumSet.of(KillChainPhase.RECON);
    /** Ataque anticipado -> carta con la que se anticipo. */
    private final Map<String, String> blockedAttacks = new HashMap<>();
    private CiaState ciaState = CiaState.intact();
    private Integer takedownRound;
    private boolean finished;
    private boolean defencesRevealed;

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

    public int defendedCia() {
        int total = 0;
        for (CiaPillar pillar : CiaPillar.values()) {
            total += ciaState.levelOf(pillar);
        }
        return total;
    }

    public List<ActiveCard> activeCards() {
        return List.copyOf(activeCards);
    }

    public List<ActiveCard> activeCardsOf(Role side) {
        return activeCards.stream().filter(card -> card.side() == side).toList();
    }

    public boolean isActive(String cardId) {
        return activeCards.stream().anyMatch(card -> card.cardId().equals(cardId));
    }

    /**
     * Una carta no se apila consigo misma: volver a desplegarla renueva su
     * duracion en lugar de duplicar su mitigacion y multiplicar su counter.
     */
    public void activate(ActiveCard card) {
        deactivate(card.cardId());
        activeCards.add(card);
    }

    public void deactivate(String cardId) {
        activeCards.removeIf(card -> card.cardId().equals(cardId));
    }

    public boolean isUnlocked(KillChainPhase phase) {
        return unlockedPhases.contains(phase);
    }

    public void unlock(KillChainPhase phase) {
        unlockedPhases.add(phase);
    }

    public Set<KillChainPhase> unlockedPhases() {
        return Set.copyOf(unlockedPhases);
    }

    /** El atacante ha averiguado que defensas hay puestas. */
    public boolean areDefencesRevealed() {
        return defencesRevealed;
    }

    public void revealDefences() {
        this.defencesRevealed = true;
    }

    /** El defensor anticipa un ataque concreto, que fallara cuando llegue. */
    public void blockAttack(String attackId, String withCardId) {
        blockedAttacks.put(attackId, withCardId);
    }

    public boolean isBlocked(String cardId) {
        return blockedAttacks.containsKey(cardId);
    }

    public String blockedBy(String cardId) {
        return blockedAttacks.get(cardId);
    }

    public String consumeBlock(String cardId) {
        return blockedAttacks.remove(cardId);
    }

    public void addBudget(TeamId team, int amount) {
        budgets.merge(team, amount, Integer::sum);
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

    public void advanceRound(Map<TeamId, Integer> catchUpBonus) {
        rounds.add(new Round(currentRound().number() + 1));
        // El presupuesto no gastado se acumula: ahorrar para una accion cara
        // es una decision valida.
        budgets.replaceAll((team, budget) -> budget + settings.incomePerRound());
        catchUpBonus.forEach(this::addBudget);
        expireCards();
    }

    private void expireCards() {
        List<ActiveCard> survivors = activeCards.stream()
                .map(ActiveCard::afterRound)
                .filter(card -> !card.isExpired())
                .toList();
        activeCards.clear();
        activeCards.addAll(survivors);
    }

    public void finish() {
        this.finished = true;
    }
}
