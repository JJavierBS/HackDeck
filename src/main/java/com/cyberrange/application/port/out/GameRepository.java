package com.cyberrange.application.port.out;

import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.JoinCode;

import java.util.List;
import java.util.Optional;

public interface GameRepository {

    Game save(Game game);

    Optional<Game> findById(GameId gameId);

    Optional<Game> findByJoinCode(JoinCode joinCode);

    boolean existsByJoinCode(JoinCode joinCode);

    List<Game> findInProgress();
}
