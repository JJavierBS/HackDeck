package com.cyberrange.adapter.persistence.memory;

import com.cyberrange.application.port.out.GameRepository;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.JoinCode;
import org.springframework.stereotype.Repository;

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
}
