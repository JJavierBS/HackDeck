package com.hackdeck.application.port.in;

import com.hackdeck.domain.model.JoinCode;
import com.hackdeck.domain.model.TournamentId;

public record TournamentAccess(TournamentId tournamentId, JoinCode joinCode, String displayName, String token) {
}
