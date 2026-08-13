package com.cyberrange.adapter.persistence.memory;

import com.cyberrange.application.port.out.TournamentRepository;
import com.cyberrange.domain.model.JoinCode;
import com.cyberrange.domain.model.Tournament;
import com.cyberrange.domain.model.TournamentId;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public final class InMemoryTournamentRepository implements TournamentRepository {

    private final Map<TournamentId, Tournament> torneos = new ConcurrentHashMap<>();

    @Override
    public Tournament save(Tournament tournament) {
        torneos.put(tournament.id(), tournament);
        return tournament;
    }

    @Override
    public Optional<Tournament> findById(TournamentId id) {
        return Optional.ofNullable(torneos.get(id));
    }

    @Override
    public Optional<Tournament> findByJoinCode(JoinCode joinCode) {
        return torneos.values().stream().filter(t -> t.joinCode().equals(joinCode)).findFirst();
    }
}
