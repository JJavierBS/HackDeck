import { useCallback, useEffect, useState } from "react";
import { createRealtimeClient } from "./realtimeClient";
import type { GameStateDto, RestClient } from "./restClient";
import type { GameSession } from "./session";

interface GameStateHandle {
  state: GameStateDto | null;
  /**
   * Vuelve a pedir el estado. Hace falta tras encolar: el servidor no
   * difunde nada en ese momento a proposito, porque los turnos son
   * simultaneos a ciegas, asi que sin esto el equipo pulsa y no ve nada.
   */
  refresh: () => void;
}

/**
 * El estado llega ya filtrado por rol desde el servidor, asi que el
 * mensaje del WebSocket se usa tal cual. El REST solo se consulta al
 * conectar, al reconectar y cuando uno mismo cambia algo.
 */
export function useGameState(client: RestClient, session: GameSession): GameStateHandle {
  const [state, setState] = useState<GameStateDto | null>(null);

  const refresh = useCallback(() => {
    client
      .getGameState(session)
      .then(setState)
      .catch(() => {});
  }, [client, session]);

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

  return { state, refresh };
}
