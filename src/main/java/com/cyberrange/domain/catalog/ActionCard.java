package com.cyberrange.domain.catalog;

import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.Role;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Definicion de una carta tal y como viene del catalogo externo. Es un dato
 * de configuracion, no estado de partida: describe lo que la carta puede
 * hacer, y es el motor de reglas quien decide que ocurre al jugarla.
 *
 * @param side       bando que puede jugarla; vacio en las cartas del instructor.
 * @param phase      fase de kill chain a la que pertenece (solo atacante).
 * @param category   capa defensiva a la que pertenece (solo defensor).
 * @param impact     delta por pilar; negativo hace dano y positivo repara.
 * @param mitigation reduccion generica de dano que aporta mientras dure.
 * @param detection  cuanto sube el nivel de deteccion del defensor.
 * @param counters   carta rival -> multiplicador que le queda tras el contra.
 *                   Contra una carta de IMPACT recorta el dano; contra el
 *                   resto, la probabilidad de acierto.
 * @param requires   cartas propias que deben estar activas para poder jugarla.
 * @param unlocks    fases de kill chain que habilita al tener exito.
 * @param bonus      cuanto sube la probabilidad de acierto de otras cartas.
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
