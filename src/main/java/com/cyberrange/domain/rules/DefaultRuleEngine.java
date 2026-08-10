package com.cyberrange.domain.rules;

import com.cyberrange.domain.model.ActionIntent;
import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.CiaState;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameEvent;
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

/**
 * Reglas provisionales para poder jugar una partida entera de punta a punta.
 * Los numeros viven aqui solo hasta la fase 2 del roadmap, que los sacara al
 * catalogo externo; y falta todo lo de la fase 3: kill chain, counters
 * especificos, deteccion con suerte, presupuesto y catch-up.
 */
public final class DefaultRuleEngine implements RuleEngine {

    /** Dano base de una accion ofensiva sobre el pilar que ataca. */
    static final int ATTACK_IMPACT = 15;

    /** Dano que atraviesa siempre: defenderse reduce el golpe, no lo anula. */
    static final int MIN_ATTACK_IMPACT = 3;

    /** Cada defensa repara un poco su pilar y lo escuda durante esta ronda. */
    static final int DEFENSE_REPAIR = 5;
    static final int DEFENSE_SHIELD = 5;

    /** Pilar al que apunta una accion que no dice a cual. */
    static final CiaPillar DEFAULT_PILLAR = CiaPillar.AVAILABILITY;

    /** Coste plano mientras el catalogo externo no diga lo que vale cada accion. */
    static final int ACTION_COST = 5;

    private static final String PILLAR_PARAMETER = "pillar";

    @Override
    public RoundResolution resolveRound(Game game, Round round) {
        List<GameEvent> events = new ArrayList<>();
        CiaState state = game.ciaState();

        // Las defensas se resuelven primero para que protejan de los ataques
        // de esta misma ronda: si no, defenderse siempre llegaria tarde.
        Map<CiaPillar, Integer> shields = new EnumMap<>(CiaPillar.class);
        for (ActionIntent action : actionsOf(round, Role.DEFENDER)) {
            CiaPillar pillar = targetPillarOf(action);
            shields.merge(pillar, DEFENSE_SHIELD, Integer::sum);
            state = state.withImpact(pillar, DEFENSE_REPAIR);
            events.add(defenseEvent(round, action, pillar));
        }

        for (ActionIntent action : actionsOf(round, Role.ATTACKER)) {
            CiaPillar pillar = targetPillarOf(action);
            int damage = damageAfterShield(shields.getOrDefault(pillar, 0));
            state = state.withImpact(pillar, -damage);
            events.add(attackEvent(round, action, pillar, damage));
        }

        return new RoundResolution(state, events, firstDownedPillar(state) != null);
    }

    @Override
    public int costOf(ActionIntent action) {
        return ACTION_COST;
    }

    /**
     * Marcador del match. Derribar un pilar manda sobre cualquier otra
     * consideracion; si los dos lo consiguen gana quien tardo menos rondas,
     * y si nadie derriba gana quien mejor defendio su triada.
     */
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

    private static int damageAfterShield(int shield) {
        return Math.max(ATTACK_IMPACT - shield, MIN_ATTACK_IMPACT);
    }

    private static List<ActionIntent> actionsOf(Round round, Role side) {
        return round.queuedActions().stream().filter(action -> action.team() == side).toList();
    }

    /**
     * Deteccion provisional: al defensor solo se le revela lo que hace ruido.
     * La fase 3 lo cambiara por umbral de deteccion mas un factor de suerte.
     */
    private static GameEvent attackEvent(Round round, ActionIntent action, CiaPillar pillar, int damage) {
        return new GameEvent(
                round.number(),
                Role.ATTACKER,
                "Ataque " + action.actionType() + " sobre " + pillar + " (-" + damage + ")",
                action.noisy(),
                Instant.now());
    }

    private static GameEvent defenseEvent(Round round, ActionIntent action, CiaPillar pillar) {
        return new GameEvent(
                round.number(),
                Role.DEFENDER,
                "Defensa " + action.actionType() + " sobre " + pillar,
                true,
                Instant.now());
    }

    private static CiaPillar targetPillarOf(ActionIntent action) {
        String raw = action.parameters().get(PILLAR_PARAMETER);
        if (raw == null) {
            return DEFAULT_PILLAR;
        }
        try {
            return CiaPillar.valueOf(raw.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT_PILLAR;
        }
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
