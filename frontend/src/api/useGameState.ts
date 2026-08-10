import { useEffect, useState } from "react";
import { createRealtimeClient } from "./realtimeClient";
import type { GameStateDto, RestClient } from "./restClient";
import type { GameSession } from "./session";

/**
 * El estado llega ya filtrado por rol desde el servidor, asi que el
 * mensaje del WebSocket se usa tal cual. El REST solo se consulta al
 * conectar y al reconectar, para reconciliar lo que se haya perdido
 * mientras el socket estuvo caido.
 */
export function useGameState(client: RestClient, session: GameSession): GameStateDto | null {
  const [state, setState] = useState<GameStateDto | null>(null);

  useEffect(() => {
    let active = true;
    const apply = (next: GameStateDto) => {
      if (active) {
        setState(next);
      }
    };

    const realtime = createRealtimeClient();
    realtime.onOpen(() => {
      client.getGameState(session).then(apply).catch(() => {});
    });
    realtime.onMessage((message) => {
      if (message.type === "state") {
        apply(message.state as GameStateDto);
      }
    });
    realtime.connect(session);

    return () => {
      active = false;
      realtime.disconnect();
    };
  }, [client, session]);

  return state;
}
