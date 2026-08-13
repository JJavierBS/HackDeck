package com.hackdeck.application.port.out;

import com.hackdeck.domain.model.JoinCode;
import com.hackdeck.domain.model.Tournament;
import com.hackdeck.domain.model.TournamentId;

import java.util.Optional;

public interface TournamentRepository {

    Tournament save(Tournament tournament);

    Optional<Tournament> findById(TournamentId id);

    Optional<Tournament> findByJoinCode(JoinCode joinCode);
}
