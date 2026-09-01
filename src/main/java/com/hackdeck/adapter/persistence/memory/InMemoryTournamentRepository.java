package com.hackdeck.adapter.persistence.memory;

import com.hackdeck.application.port.out.TournamentRepository;
import com.hackdeck.domain.model.JoinCode;
import com.hackdeck.domain.model.Tournament;
import com.hackdeck.domain.model.TournamentId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public final class InMemoryTournamentRepository implements TournamentRepository {

    private final Map<TournamentId, Tournament> torneos = new ConcurrentHashMap<>();
    // Indice propio: buscar el codigo recorriendo todos los torneos convertia
    // cada intento de union anonimo en un barrido del mapa entero.
    private final Map<JoinCode, TournamentId> torneosPorCodigo = new ConcurrentHashMap<>();

    @Override
    public Tournament save(Tournament tournament) {
        torneos.put(tournament.id(), tournament);
        torneosPorCodigo.put(tournament.joinCode(), tournament.id());
        return tournament;
    }

    @Override
    public Optional<Tournament> findById(TournamentId id) {
        return Optional.ofNullable(torneos.get(id));
    }

    @Override
    public Optional<Tournament> findByJoinCode(JoinCode joinCode) {
        return Optional.ofNullable(torneosPorCodigo.get(joinCode)).flatMap(this::findById);
    }

    @Override
    public List<Tournament> findAll() {
        return List.copyOf(torneos.values());
    }

    @Override
    public int count() {
        return torneos.size();
    }

    @Override
    public void delete(TournamentId id) {
        Tournament borrado = torneos.remove(id);
        if (borrado != null) {
            torneosPorCodigo.remove(borrado.joinCode(), id);
        }
    }
}
