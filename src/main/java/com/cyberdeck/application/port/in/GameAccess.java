package com.cyberdeck.application.port.in;

import com.cyberdeck.domain.model.GameId;
import com.cyberdeck.domain.model.JoinCode;
import com.cyberdeck.domain.model.Participant;

public record GameAccess(GameId gameId, JoinCode joinCode, Participant participant, String token) {
}
