package com.hackdeck.domain.model;

import java.util.Objects;

/**
 * Identidad ya verificada de quien hace una peticion: a que partida
 * pertenece y en calidad de que. Es lo que el backend usa para autorizar,
 * nunca lo que diga el cliente en el cuerpo de la peticion.
 */
public record ParticipantSession(GameId gameId, Participant participant) {

    public ParticipantSession {
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(participant, "participant");
    }

    public boolean belongsTo(GameId otherGameId) {
        return gameId.equals(otherGameId);
    }

    public boolean isInstructor() {
        return participant.isInstructor();
    }
}
