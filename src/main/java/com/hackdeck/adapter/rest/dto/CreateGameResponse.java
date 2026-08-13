package com.hackdeck.adapter.rest.dto;

import com.hackdeck.application.port.in.GameAccess;

public record CreateGameResponse(String gameId, String joinCode, String team, String token) {

    public static CreateGameResponse from(GameAccess access) {
        return new CreateGameResponse(
                access.gameId().toString(),
                access.joinCode().toString(),
                access.participant().team() == null ? null : access.participant().team().name(),
                access.token());
    }
}
