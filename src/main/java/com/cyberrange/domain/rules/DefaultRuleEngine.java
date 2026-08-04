package com.cyberrange.domain.rules;

import com.cyberrange.domain.model.ActionIntent;
import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.CiaState;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameEvent;
import com.cyberrange.domain.model.Role;
import com.cyberrange.domain.model.Round;

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

        CiaPillar downedPillar = firstDownedPillar(state);
        return new RoundResolution(
                state,
                events,
                downedPillar != null,
                downedPillar == null ? null : Role.ATTACKER);
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
                "Ataque " + action.actionType() + " sobre " + pillar + " (-" + damage + ")",
                action.noisy(),
                Instant.now());
    }

    private static GameEvent defenseEvent(Round round, ActionIntent action, CiaPillar pillar) {
        return new GameEvent(
                round.number(),
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
