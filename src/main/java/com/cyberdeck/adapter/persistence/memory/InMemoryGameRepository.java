package com.cyberdeck.adapter.persistence.memory;

import com.cyberdeck.application.port.out.GameRepository;
import com.cyberdeck.domain.model.Game;
import com.cyberdeck.domain.model.GameId;
import com.cyberdeck.domain.model.GamePhase;
import com.cyberdeck.domain.model.JoinCode;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public final class InMemoryGameRepository implements GameRepository {

    private final Map<GameId, Game> games = new ConcurrentHashMap<>();
    private final Map<JoinCode, GameId> gamesByJoinCode = new ConcurrentHashMap<>();

    @Override
    public Game save(Game game) {
        games.put(game.id(), game);
        gamesByJoinCode.put(game.joinCode(), game.id());
        return game;
    }

    @Override
    public Optional<Game> findById(GameId gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    @Override
    public Optional<Game> findByJoinCode(JoinCode joinCode) {
        return Optional.ofNullable(gamesByJoinCode.get(joinCode)).flatMap(this::findById);
    }

    @Override
    public boolean existsByJoinCode(JoinCode joinCode) {
        return gamesByJoinCode.containsKey(joinCode);
    }

    @Override
    public List<Game> findInProgress() {
        return games.values().stream().filter(game -> game.phase() == GamePhase.IN_PROGRESS).toList();
    }
}
