import { useEffect, useState } from "react";
import { createRealtimeClient } from "./realtimeClient";
import type { GameStateDto, RestClient } from "./restClient";
import type { GameSession } from "./session";

/**
 * El estado autorizado viene por REST; el WebSocket solo avisa de que hay
 * novedades, para no depender de un canal que aun no filtra por rol.
 */
export function useGameState(client: RestClient, session: GameSession): GameStateDto | null {
  const [state, setState] = useState<GameStateDto | null>(null);

  useEffect(() => {
    let active = true;

    const refresh = () => {
      client
        .getGameState(session)
        .then((next) => {
          if (active) {
            setState(next);
          }
        })
        .catch(() => {});
    };

    refresh();

    const realtime = createRealtimeClient();
    realtime.onMessage(refresh);
    realtime.connect(session);

    return () => {
      active = false;
      realtime.disconnect();
    };
  }, [client, session]);

  return state;
}
