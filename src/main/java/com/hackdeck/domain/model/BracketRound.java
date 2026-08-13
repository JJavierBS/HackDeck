package com.hackdeck.domain.model;

import java.util.List;

public record BracketRound(int number, List<Pairing> pairings) {

    public BracketRound {
        pairings = List.copyOf(pairings);
    }

    public boolean isComplete() {
        return pairings.stream().allMatch(pairing -> pairing.winner().isPresent());
    }

    public List<PlayerId> winners() {
        return pairings.stream().flatMap(pairing -> pairing.winner().stream()).toList();
    }
}
