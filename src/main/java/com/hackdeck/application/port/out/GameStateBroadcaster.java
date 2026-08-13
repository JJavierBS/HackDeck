package com.hackdeck.application.port.out;

import com.hackdeck.domain.model.Game;
import com.hackdeck.domain.model.GameId;

public interface GameStateBroadcaster {

    void broadcastState(GameId gameId, Game game);
}
