package com.cyberdeck.application.port.in;

import com.cyberdeck.domain.model.JoinCode;
import com.cyberdeck.domain.model.TournamentId;

public record TournamentAccess(TournamentId tournamentId, JoinCode joinCode, String displayName, String token) {
}
