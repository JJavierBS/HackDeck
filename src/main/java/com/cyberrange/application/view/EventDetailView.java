package com.cyberrange.application.view;

import java.util.List;
import java.util.Map;

/**
 * Lo que le paso a una accion, para poder explicarselo a quien la jugo.
 * counteredBy llega vacio al atacante: saber que defensas tiene el rival es
 * justo lo que se paga con una carta de reconocimiento.
 */
public record EventDetailView(
        Boolean success,
        String failureReason,
        Map<String, Integer> impact,
        int mitigated,
        List<String> unlocked,
        List<String> boosts,
        Boolean detected,
        String counteredBy) {
}
