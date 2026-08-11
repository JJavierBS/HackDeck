import { useEffect, useState } from "react";
import type { CardDto, RestClient } from "./restClient";
import type { GameSession } from "./session";

/**
 * El catalogo se pide de nuevo en cada mitad porque al cambiar de bando el
 * equipo pasa a jugar con las cartas contrarias.
 */
export function useCatalog(client: RestClient, session: GameSession, half: number | null): CardDto[] {
  const [cards, setCards] = useState<CardDto[]>([]);

  useEffect(() => {
    if (half === null) {
      return;
    }
    let active = true;
    client
      .getCatalog(session)
      .then((loaded) => {
        if (active) {
          setCards(loaded);
        }
      })
      .catch(() => {});
    return () => {
      active = false;
    };
  }, [client, session, half]);

  return cards;
}
