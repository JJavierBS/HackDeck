package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.JoinCode;
import com.cyberrange.domain.model.Participant;

public record GameAccess(GameId gameId, JoinCode joinCode, Participant participant, String token) {
}
