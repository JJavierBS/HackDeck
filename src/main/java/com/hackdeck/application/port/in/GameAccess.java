package com.hackdeck.application.port.in;

import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.JoinCode;
import com.hackdeck.domain.model.Participant;

public record GameAccess(GameId gameId, JoinCode joinCode, Participant participant, String token) {
}
