package com.cyberdeck.application.port.out;

import com.cyberdeck.domain.model.Game;
import com.cyberdeck.domain.model.GameId;

public interface GameStateBroadcaster {

    void broadcastState(GameId gameId, Game game);
}
