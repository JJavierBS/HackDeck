package com.cyberdeck.domain.catalog;

import com.cyberdeck.domain.model.CiaPillar;
import com.cyberdeck.domain.model.Role;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Carta tal y como viene del catalogo externo: describe lo que puede hacer,
 * no lo que ocurre al jugarla.
 *
 * En impact, negativo hace dano y positivo repara. counters va de carta rival
 * al multiplicador que le queda tras el contra: contra una carta de IMPACT
 * recorta el dano y contra el resto la probabilidad de acierto.
 */
public record ActionCard(
        String id,
        CardType type,
        Role side,
        KillChainPhase phase,
        DefenseCategory category,
        Map<String, String> name,
        Map<String, String> description,
        int cost,
        NoiseLevel noise,
        double successRate,
        CardDuration duration,
        int rounds,
        Map<CiaPillar, Integer> impact,
        int mitigation,
        int detection,
        Map<String, Double> counters,
        List<String> requires,
        Set<KillChainPhase> unlocks,
        Map<String, Double> bonus,
        Set<CardEffect> effects) {

    public ActionCard {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(noise, "noise");
        Objects.requireNonNull(duration, "duration");
        name = Map.copyOf(name);
        description = description == null ? Map.of() : Map.copyOf(description);
        impact = impact == null ? Map.of() : Map.copyOf(impact);
        counters = counters == null ? Map.of() : Map.copyOf(counters);
        requires = requires == null ? List.of() : List.copyOf(requires);
        unlocks = unlocks == null ? Set.of() : Set.copyOf(unlocks);
        bonus = bonus == null ? Map.of() : Map.copyOf(bonus);
        effects = effects == null ? Set.of() : Set.copyOf(effects);
    }

    public boolean isPlayableBy(Role role) {
        return side == role;
    }

    public boolean hasEffect(CardEffect effect) {
        return effects.contains(effect);
    }

    public String nameIn(String language) {
        return name.getOrDefault(language, name.values().iterator().next());
    }
}
