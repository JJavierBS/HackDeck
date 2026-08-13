package com.cyberrange.application.port.out;

import com.cyberrange.domain.model.JoinCode;
import com.cyberrange.domain.model.Tournament;
import com.cyberrange.domain.model.TournamentId;

import java.util.Optional;

public interface TournamentRepository {

    Tournament save(Tournament tournament);

    Optional<Tournament> findById(TournamentId id);

    Optional<Tournament> findByJoinCode(JoinCode joinCode);
}
