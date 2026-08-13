package com.hackdeck.adapter.rest.dto;

import com.hackdeck.application.port.in.TournamentAccess;

public record TournamentAccessResponse(String tournamentId, String joinCode, String token) {

    public static TournamentAccessResponse from(TournamentAccess access) {
        return new TournamentAccessResponse(
                access.tournamentId().toString(), access.joinCode().toString(), access.token());
    }
}
