package com.cyberdeck.application.port.out;

import com.cyberdeck.domain.model.JoinCode;
import com.cyberdeck.domain.model.Tournament;
import com.cyberdeck.domain.model.TournamentId;

import java.util.Optional;

public interface TournamentRepository {

    Tournament save(Tournament tournament);

    Optional<Tournament> findById(TournamentId id);

    Optional<Tournament> findByJoinCode(JoinCode joinCode);
}
