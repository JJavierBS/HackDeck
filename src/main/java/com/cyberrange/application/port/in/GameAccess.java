package com.cyberrange.application.port.in;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.JoinCode;
import com.cyberrange.domain.model.Participant;

/**
 * Credenciales devueltas al crear una partida o al unirse a ella: quien eres
 * y el token con el que lo demuestras en las siguientes peticiones.
 */
public record GameAccess(GameId gameId, JoinCode joinCode, Participant participant, String token) {
}
