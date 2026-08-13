package com.cyberrange.application.port.out;

import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameId;

public interface GameStateBroadcaster {

    void broadcastState(GameId gameId, Game game);
}
