package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.JoinCode;
import com.cyberrange.domain.model.TournamentId;

public record TournamentAccess(TournamentId tournamentId, JoinCode joinCode, String displayName, String token) {
}
