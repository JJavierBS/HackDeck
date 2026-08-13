package com.cyberrange.domain.model;

import com.cyberrange.domain.catalog.KillChainPhase;

import java.util.List;
import java.util.Map;

/**
 * Consecuencias mecanicas de una accion, aparte del texto. Sirven para
 * contarle a quien la jugo que ha pasado de verdad con ella y por que.
 *
 * @param impact      cambio por pilar; negativo hace dano y positivo repara.
 * @param mitigated   cuanto absorbieron las defensas del golpe.
 * @param unlocked    fases de kill chain que abre al acertar.
 * @param boosts      cartas propias cuya probabilidad sube a partir de ahora.
 * @param counteredBy carta del rival que lo freno. El proyector se la oculta
 *                    al atacante: averiguar las defensas del rival es lo que
 *                    se paga con una carta de reconocimiento.
 */
public record EventDetail(
        Boolean success,
        FailureReason failureReason,
        Map<CiaPillar, Integer> impact,
        int mitigated,
        List<String> unlocked,
        List<String> boosts,
        Boolean detected,
        String counteredBy) {

    public EventDetail {
        impact = impact == null ? Map.of() : Map.copyOf(impact);
        unlocked = unlocked == null ? List.of() : List.copyOf(unlocked);
        boosts = boosts == null ? List.of() : List.copyOf(boosts);
    }

    public static EventDetail attack(
            boolean success,
            FailureReason failureReason,
            Map<CiaPillar, Integer> impact,
            int mitigated,
            List<KillChainPhase> unlocked,
            List<String> boosts,
            boolean detected,
            String counteredBy) {
        return new EventDetail(
                success,
                failureReason,
                impact,
                mitigated,
                unlocked.stream().map(KillChainPhase::name).toList(),
                boosts,
                detected,
                counteredBy);
    }

    public static EventDetail defence(Map<CiaPillar, Integer> repaired) {
        return new EventDetail(true, null, repaired, 0, List.of(), List.of(), null, null);
    }

    /** Sin la carta que lo freno: es lo que no debe ver el atacante. */
    public EventDetail withoutCounterName() {
        return new EventDetail(success, failureReason, impact, mitigated, unlocked, boosts, detected, null);
    }

    /**
     * Lo que se ve de una accion del rival: que paso y cuanto dolio, pero no
     * lo que a el le aporto. Su avance por la kill chain es cosa suya.
     */
    public EventDetail asRivalAction() {
        return new EventDetail(success, failureReason, impact, mitigated, List.of(), List.of(), null, counteredBy);
    }
}
