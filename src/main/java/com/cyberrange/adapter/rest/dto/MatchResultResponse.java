package com.cyberrange.adapter.rest.dto;

import com.cyberrange.domain.model.MatchResult;
import com.cyberrange.domain.model.TeamId;

import java.util.LinkedHashMap;
import java.util.Map;

public record MatchResultResponse(
        String winner,
        String outcome,
        Map<String, Integer> defendedCia,
        Map<String, Integer> takedownRound) {

    public static MatchResultResponse from(MatchResult result) {
        return new MatchResultResponse(
                result.winner() == null ? null : result.winner().name(),
                result.outcome().name(),
                byTeamName(result.defendedCia()),
                byTeamName(result.takedownRound()));
    }

    private static Map<String, Integer> byTeamName(Map<TeamId, Integer> values) {
        Map<String, Integer> byName = new LinkedHashMap<>();
        values.forEach((team, value) -> byName.put(team.name(), value));
        return byName;
    }
}
