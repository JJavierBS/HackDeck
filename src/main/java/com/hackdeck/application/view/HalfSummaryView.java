package com.hackdeck.application.view;

import java.util.Map;

/**
 * Como acabo una mitad ya cerrada. En la segunda, el nuevo defensor necesita
 * saber que marca tiene que batir: el match se decide por triada defendida.
 */
public record HalfSummaryView(
        int number,
        String attackingTeam,
        String defendingTeam,
        Map<String, Integer> ciaLevels,
        int defendedCia,
        Integer takedownRound) {
}
