package com.cyberrange.domain.rules;

import com.cyberrange.domain.catalog.ActionCard;
import com.cyberrange.domain.catalog.ActionCatalog;
import com.cyberrange.domain.catalog.CardDuration;
import com.cyberrange.domain.catalog.CardEffect;
import com.cyberrange.domain.catalog.CardType;
import com.cyberrange.domain.catalog.DefenseCategory;
import com.cyberrange.domain.catalog.KillChainPhase;
import com.cyberrange.domain.catalog.NoiseLevel;
import com.cyberrange.domain.exception.UnknownCardException;
import com.cyberrange.domain.model.ActionIntent;
import com.cyberrange.domain.model.ActiveCard;
import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.CiaState;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameEvent;
import com.cyberrange.domain.model.GameEventType;
import com.cyberrange.domain.model.Half;
import com.cyberrange.domain.model.MatchOutcome;
import com.cyberrange.domain.model.MatchResult;
import com.cyberrange.domain.model.Role;
import com.cyberrange.domain.model.Round;
import com.cyberrange.domain.model.TeamId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Motor de reglas sobre el catalogo externo: los numeros de cada carta
 * salen del YAML y aqui solo vive como se combinan.
 */
public final class DefaultRuleEngine implements RuleEngine {

    /**
     * Lo que le queda a un ataque lanzado sin haber recorrido la kill chain.
     * Atacar directamente esta permitido, pero sale caro en probabilidad:
     * es lo que empuja a pasar por reconocimiento y acceso.
     */
    static final double KILL_CHAIN_PENALTY = 0.30;

    /** Dano que siempre atraviesa: defenderse reduce el golpe, no lo anula. */
    static final int MIN_DAMAGE = 3;

    /** Probabilidad de que la deteccion salga al reves de lo esperado. */
    static final double DETECTION_LUCK = 0.15;

    /** Descuento de las cartas de respuesta cuando hay plan de respuesta. */
    static final double RESPONSE_DISCOUNT = 0.5;

    /** Dano por ronda que se considera "normal" para medir quien va perdiendo. */
    static final int EXPECTED_DAMAGE_PER_ROUND = 25;

    /** Parte del presupuesto ahorrado que se lleva un recorte. */
    static final double BUDGET_CUT_RATE = 0.3;

    static final double CATCH_UP_RATE = 0.2;
    static final int CATCH_UP_MAX = 10;

    private static final String PILLAR_PARAMETER = "pillar";

    private final ActionCatalog catalog;
    private final Randomizer randomizer;

    public DefaultRuleEngine(ActionCatalog catalog, Randomizer randomizer) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.randomizer = Objects.requireNonNull(randomizer, "randomizer");
    }

    @Override
    public ActionCard cardFor(Role side, String cardId) {
        ActionCard card = catalog.find(cardId)
                .orElseThrow(() -> new UnknownCardException("No existe ninguna carta con el id '" + cardId + "'"));
        if (!card.isPlayableBy(side)) {
            throw new UnknownCardException("La carta '" + cardId + "' no la puede jugar el bando " + side);
        }
        return card;
    }

    @Override
    public ActionCard twistFor(String cardId) {
        ActionCard card = card(cardId);
        if (card.type() != CardType.TWIST) {
            throw new UnknownCardException("La carta '" + cardId + "' no es un evento twist");
        }
        return card;
    }

    @Override
    public Map<TeamId, Integer> twistBudgetChange(Game game, ActionCard twist) {
        if (!twist.hasEffect(CardEffect.BUDGET_CUT)) {
            return Map.of();
        }
        Half half = game.currentHalf();
        Map<TeamId, Integer> changes = new EnumMap<>(TeamId.class);
        for (TeamId team : TeamId.values()) {
            changes.put(team, -(int) Math.round(half.budgetOf(team) * BUDGET_CUT_RATE));
        }
        return changes;
    }

    @Override
    public int costOf(Game game, ActionCard card) {
        boolean discounted = card.category() == DefenseCategory.RESPONSE
                && game.currentHalf().activeCardsOf(Role.DEFENDER).stream()
                        .anyMatch(active -> hasEffect(active.cardId(), CardEffect.CHEAPER_RESPONSE));
        return discounted ? (int) Math.round(card.cost() * RESPONSE_DISCOUNT) : card.cost();
    }

    @Override
    public RoundResolution resolveRound(Game game, Round round) {
        Half half = game.currentHalf();
        List<GameEvent> events = new ArrayList<>();
        CiaState state = half.ciaState();

        // Las defensas se resuelven primero para que protejan de los ataques
        // de esta misma ronda: si no, defenderse siempre llegaria tarde.
        List<ActionCard> defences = cardsOf(round, Role.DEFENDER);
        boolean doubleRepair = anyHasEffect(defences, CardEffect.DOUBLE_REPAIR);
        for (ActionIntent intent : intentsOf(round, Role.DEFENDER)) {
            ActionCard card = cardFor(Role.DEFENDER, intent.cardId());
            state = applyDefence(half, state, card, intent, doubleRepair);
            events.add(defenceEvent(half, round, card));
        }

        int detection = detectionLevel(half, defences);
        List<ActionCard> attacks = cardsOf(round, Role.ATTACKER);
        boolean silenced = anyHasEffect(attacks, CardEffect.SILENCES_ROUND);
        boolean revealed = anyHasEffect(defences, CardEffect.REVEALS_ROUND) || twistRevealsRound(half);
        boolean ignoresCounters = anyHasEffect(attacks, CardEffect.IGNORES_COUNTERS);
        boolean doubleImpact = anyHasEffect(attacks, CardEffect.DOUBLE_IMPACT);

        for (ActionIntent intent : intentsOf(round, Role.ATTACKER)) {
            ActionCard card = cardFor(Role.ATTACKER, intent.cardId());
            double chance = successChance(half, card, ignoresCounters);
            boolean success = randomizer.chance(chance);
            int damage = 0;
            if (success) {
                card.unlocks().forEach(half::unlock);
                activateIfLasting(half, card, Role.ATTACKER);
                CiaState before = state;
                state = applyAttack(half, state, card, ignoresCounters, doubleImpact);
                damage = totalDrop(before, state);
            }
            boolean detected = revealed || detected(card.noise(), detection, silenced);
            events.add(attackEvent(half, round, card, describeAttack(card, success, damage), detected));
        }

        events.add(roundResolvedEvent(half, round, state));
        boolean takedown = firstDownedPillar(state) != null;
        return new RoundResolution(state, events, takedown, catchUpBonus(game, half, state, round));
    }

    private CiaState applyDefence(
            Half half, CiaState state, ActionCard card, ActionIntent intent, boolean doubleRepair) {
        activateIfLasting(half, card, Role.DEFENDER);
        if (card.hasEffect(CardEffect.REMOVES_PERSISTENCE)) {
            half.activeCardsOf(Role.ATTACKER).stream()
                    .filter(active -> hasEffect(active.cardId(), CardEffect.PERSISTENCE))
                    .forEach(active -> half.deactivate(active.cardId()));
        }
        if (card.hasEffect(CardEffect.EXTRA_BUDGET)) {
            half.addBudget(half.defendingTeam(), CATCH_UP_MAX);
        }
        CiaState repaired = state;
        for (Map.Entry<CiaPillar, Integer> entry : targetedImpact(card, intent).entrySet()) {
            if (entry.getValue() > 0) {
                int repair = doubleRepair ? entry.getValue() * 2 : entry.getValue();
                repaired = repaired.withImpact(entry.getKey(), repair);
            }
        }
        return repaired;
    }

    private CiaState applyAttack(
            Half half, CiaState state, ActionCard card, boolean ignoresCounters, boolean doubleImpact) {
        double factor = 1.0;
        if (!ignoresCounters) {
            factor *= counterFactor(half, card.id());
        }
        if (doubleImpact) {
            factor *= 2;
        }
        if (hasAmplifier(half)) {
            factor *= 1.5;
        }
        int mitigation = mitigation(half);

        CiaState damaged = state;
        for (Map.Entry<CiaPillar, Integer> entry : card.impact().entrySet()) {
            if (entry.getValue() >= 0) {
                continue;
            }
            int raw = (int) Math.round(Math.abs(entry.getValue()) * factor);
            int effective = Math.max(raw - mitigation, MIN_DAMAGE);
            damaged = damaged.withImpact(entry.getKey(), -effective);
        }
        return damaged;
    }

    /**
     * Los counters recortan la probabilidad de acierto de todo lo que no sea
     * un impacto. Contra un impacto no evitas que ocurra, asi que lo que
     * recortan es el dano (ver applyAttack): un backup no impide el
     * ransomware, lo vuelve soportable.
     */
    private double successChance(Half half, ActionCard card, boolean ignoresCounters) {
        double chance = card.successRate();
        if (card.phase() != null && !half.isUnlocked(card.phase())) {
            chance *= KILL_CHAIN_PENALTY;
        }
        chance += bonusFor(half, card.id());
        if (!ignoresCounters && card.phase() != KillChainPhase.IMPACT) {
            chance *= counterFactor(half, card.id());
        }
        return Math.clamp(chance, 0.0, 1.0);
    }

    /**
     * Deteccion mixta: umbral determinista mas un modificador de suerte que
     * deja pasar algo ruidoso de vez en cuando y descubre algo silencioso.
     * Lo que no hace ruido en absoluto no se detecta nunca: ocurre fuera de
     * la red del defensor.
     */
    private boolean detected(NoiseLevel noise, int detection, boolean silenced) {
        if (silenced || noise == NoiseLevel.NONE) {
            return false;
        }
        boolean expected = detection >= noise.level();
        return randomizer.chance(DETECTION_LUCK) != expected;
    }

    private Map<TeamId, Integer> catchUpBonus(Game game, Half half, CiaState state, Round round) {
        int total = 0;
        for (CiaPillar pillar : CiaPillar.values()) {
            total += state.levelOf(pillar);
        }
        int expected = Math.max(
                CiaState.MAX_LEVEL * CiaPillar.values().length - (round.number() * EXPECTED_DAMAGE_PER_ROUND),
                0);
        int difference = total - expected;
        int bonus = (int) Math.min(Math.round(Math.abs(difference) * CATCH_UP_RATE), CATCH_UP_MAX);
        if (bonus == 0) {
            return Map.of();
        }
        // Si la triada aguanta mas de lo esperado el que va perdiendo es el
        // atacante, y al reves. Cuanta mas diferencia, mas ayuda.
        TeamId behind = difference > 0 ? half.attackingTeam() : half.defendingTeam();
        return Map.of(behind, bonus);
    }

    @Override
    public MatchResult scoreMatch(Game game) {
        Map<TeamId, Integer> defendedCia = new EnumMap<>(TeamId.class);
        Map<TeamId, Integer> takedownRound = new EnumMap<>(TeamId.class);
        for (Half half : game.halves()) {
            defendedCia.put(half.defendingTeam(), half.defendedCia());
            if (half.takedownRound() != null) {
                takedownRound.put(half.attackingTeam(), half.takedownRound());
            }
        }

        Integer takedownA = takedownRound.get(TeamId.A);
        Integer takedownB = takedownRound.get(TeamId.B);

        if (takedownA != null && takedownB != null) {
            if (!takedownA.equals(takedownB)) {
                TeamId faster = takedownA < takedownB ? TeamId.A : TeamId.B;
                return new MatchResult(faster, MatchOutcome.TAKEDOWN_FASTER, defendedCia, takedownRound);
            }
            // Derribo en la misma ronda: se cae al criterio de puntos.
            return byDefendedCia(defendedCia, takedownRound);
        }
        if (takedownA != null) {
            return new MatchResult(TeamId.A, MatchOutcome.TAKEDOWN, defendedCia, takedownRound);
        }
        if (takedownB != null) {
            return new MatchResult(TeamId.B, MatchOutcome.TAKEDOWN, defendedCia, takedownRound);
        }
        return byDefendedCia(defendedCia, takedownRound);
    }

    private static MatchResult byDefendedCia(Map<TeamId, Integer> defendedCia, Map<TeamId, Integer> takedownRound) {
        int defendedByA = defendedCia.getOrDefault(TeamId.A, 0);
        int defendedByB = defendedCia.getOrDefault(TeamId.B, 0);
        if (defendedByA == defendedByB) {
            return new MatchResult(null, MatchOutcome.DRAW, defendedCia, takedownRound);
        }
        TeamId winner = defendedByA > defendedByB ? TeamId.A : TeamId.B;
        return new MatchResult(winner, MatchOutcome.POINTS, defendedCia, takedownRound);
    }

    private void activateIfLasting(Half half, ActionCard card, Role side) {
        // El reconocimiento se queda para siempre aunque sea instantaneo: lo
        // que se averigua no se olvida a la ronda siguiente.
        boolean knowledge = card.phase() == KillChainPhase.RECON;
        if (card.duration() == CardDuration.PERMANENT || knowledge) {
            half.activate(ActiveCard.permanent(card.id(), side));
        } else if (card.duration() == CardDuration.ROUNDS) {
            half.activate(new ActiveCard(card.id(), side, card.rounds()));
        }
    }

    private double counterFactor(Half half, String cardId) {
        double factor = 1.0;
        for (ActiveCard active : half.activeCards()) {
            Double remaining = card(active.cardId()).counters().get(cardId);
            if (remaining != null) {
                factor *= remaining;
            }
        }
        return factor;
    }

    private double bonusFor(Half half, String cardId) {
        double bonus = 0;
        for (ActiveCard active : half.activeCards()) {
            bonus += card(active.cardId()).bonus().getOrDefault(cardId, 0.0);
        }
        return bonus;
    }

    private int mitigation(Half half) {
        return half.activeCardsOf(Role.DEFENDER).stream()
                .mapToInt(active -> card(active.cardId()).mitigation())
                .sum();
    }

    private int detectionLevel(Half half, List<ActionCard> playedThisRound) {
        int active = half.activeCardsOf(Role.DEFENDER).stream()
                .mapToInt(card -> card(card.cardId()).detection())
                .sum();
        int instant = playedThisRound.stream()
                .filter(card -> card.duration() == CardDuration.INSTANT)
                .mapToInt(ActionCard::detection)
                .sum();
        return active + instant;
    }

    private boolean hasAmplifier(Half half) {
        return half.activeCardsOf(Role.ATTACKER).stream()
                .anyMatch(active -> hasEffect(active.cardId(), CardEffect.AMPLIFIES_IMPACT));
    }

    private boolean twistRevealsRound(Half half) {
        return half.activeCards().stream()
                .filter(active -> active.side() == null)
                .anyMatch(active -> hasEffect(active.cardId(), CardEffect.REVEALS_ROUND));
    }

    private boolean hasEffect(String cardId, CardEffect effect) {
        return card(cardId).hasEffect(effect);
    }

    private static boolean anyHasEffect(List<ActionCard> cards, CardEffect effect) {
        return cards.stream().anyMatch(card -> card.hasEffect(effect));
    }

    /**
     * Una carta con un solo pilar se puede dirigir a otro con el parametro
     * "pillar": restaurar una copia sirve para lo que haga falta.
     */
    private static Map<CiaPillar, Integer> targetedImpact(ActionCard card, ActionIntent intent) {
        String requested = intent.parameters().get(PILLAR_PARAMETER);
        if (requested == null || card.impact().size() != 1) {
            return card.impact();
        }
        try {
            CiaPillar pillar = CiaPillar.valueOf(requested.strip().toUpperCase());
            return Map.of(pillar, card.impact().values().iterator().next());
        } catch (IllegalArgumentException e) {
            return card.impact();
        }
    }

    private ActionCard card(String cardId) {
        return catalog.find(cardId)
                .orElseThrow(() -> new UnknownCardException("No existe ninguna carta con el id '" + cardId + "'"));
    }

    private List<ActionIntent> intentsOf(Round round, Role side) {
        return round.queuedActions().stream().filter(intent -> intent.team() == side).toList();
    }

    private List<ActionCard> cardsOf(Round round, Role side) {
        return intentsOf(round, side).stream().map(intent -> card(intent.cardId())).toList();
    }

    private static int totalDrop(CiaState before, CiaState after) {
        int drop = 0;
        for (CiaPillar pillar : CiaPillar.values()) {
            drop += before.levelOf(pillar) - after.levelOf(pillar);
        }
        return drop;
    }

    private static GameEvent attackEvent(
            Half half, Round round, ActionCard card, String description, boolean detected) {
        return GameEvent.byCard(
                half.number(), round.number(), GameEventType.ATTACK, Role.ATTACKER, card.id(), description, detected);
    }

    private static GameEvent defenceEvent(Half half, Round round, ActionCard card) {
        return GameEvent.byCard(
                half.number(),
                round.number(),
                GameEventType.DEFENCE,
                Role.DEFENDER,
                card.id(),
                describeDefence(card),
                true);
    }

    /**
     * Cierra la ronda con la foto de la triada. Es lo que permite rebobinar
     * el match despues sin volver a pasar las reglas.
     */
    private static GameEvent roundResolvedEvent(Half half, Round round, CiaState state) {
        Map<CiaPillar, Integer> snapshot = new EnumMap<>(CiaPillar.class);
        for (CiaPillar pillar : CiaPillar.values()) {
            snapshot.put(pillar, state.levelOf(pillar));
        }
        return new GameEvent(
                half.number(),
                round.number(),
                GameEventType.ROUND_RESOLVED,
                null,
                null,
                "Se resuelve la ronda " + round.number(),
                true,
                snapshot,
                Instant.now());
    }

    private static String describeAttack(ActionCard card, boolean success, int damage) {
        String name = card.nameIn("es");
        if (!success) {
            return name + " fracasa";
        }
        return damage > 0 ? name + " tiene exito (-" + damage + ")" : name + " tiene exito";
    }

    private static String describeDefence(ActionCard card) {
        return card.nameIn("es") + " desplegada";
    }

    private static CiaPillar firstDownedPillar(CiaState state) {
        for (CiaPillar pillar : CiaPillar.values()) {
            if (state.isPillarDown(pillar)) {
                return pillar;
            }
        }
        return null;
    }
}
