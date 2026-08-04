package com.cyberrange.adapter.rest.dto;

import com.cyberrange.application.port.in.GameAccess;

/**
 * Credenciales de acceso. El campo team viene vacio para el instructor.
 */
public record CreateGameResponse(String gameId, String joinCode, String team, String token) {

    public static CreateGameResponse from(GameAccess access) {
        return new CreateGameResponse(
                access.gameId().toString(),
                access.joinCode().toString(),
                access.participant().team() == null ? null : access.participant().team().name(),
                access.token());
    }
}
