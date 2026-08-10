package com.cyberrange.application.port.out;

import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameId;

/**
 * Puerto de salida: difusion en tiempo real del estado de una partida a los
 * participantes conectados. Los eventos viajan dentro del estado, ya
 * filtrados por rol, para que no haya dos caminos distintos por los que se
 * pueda escapar informacion.
 */
public interface GameStateBroadcaster {

    void broadcastState(GameId gameId, Game game);
}
