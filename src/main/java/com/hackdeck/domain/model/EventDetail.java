package com.hackdeck.domain.model;

import com.hackdeck.domain.catalog.KillChainPhase;

import java.util.List;
import java.util.Map;

/**
 * Consecuencias mecanicas de una accion, para contarle a quien la jugo que
 * ha pasado y por que. counteredBy no le llega al atacante: averiguar las
 * defensas del rival es lo que se paga con una carta de reconocimiento.
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
