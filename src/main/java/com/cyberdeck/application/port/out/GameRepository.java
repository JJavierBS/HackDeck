package com.cyberdeck.application.port.out;

import com.cyberdeck.domain.model.Game;
import com.cyberdeck.domain.model.GameId;
import com.cyberdeck.domain.model.JoinCode;

import java.util.List;
import java.util.Optional;

public interface GameRepository {

    Game save(Game game);

    Optional<Game> findById(GameId gameId);

    Optional<Game> findByJoinCode(JoinCode joinCode);

    boolean existsByJoinCode(JoinCode joinCode);

    List<Game> findInProgress();
}
