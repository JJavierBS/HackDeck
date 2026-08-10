import type { GameStateDto } from "../api/restClient";

const STATUS_TEXT: Record<string, string> = {
  INTACT: "intacto",
  DAMAGED: "tocado",
  CRITICAL: "critico",
  DOWN: "caido",
};

/**
 * Triada, cola propia y registro, pintados con lo que el rol haya
 * recibido: el atacante no tiene valores exactos que mostrar.
 */
export function GameFeed({ state }: { state: GameStateDto }) {
  const pillars = state.ciaLevels ?? state.ciaStatus;

  return (
    <>
      <section>
        <h2>Triada</h2>
        <ul>
          {Object.entries(pillars ?? {}).map(([pillar, value]) => (
            <li key={pillar}>
              {pillar}: {typeof value === "number" ? value : STATUS_TEXT[value]}
            </li>
          ))}
        </ul>
      </section>

      <section>
        <h2>Tu cola de esta ronda</h2>
        {state.yourQueuedActions.length === 0 ? (
          <p>Nada encolado todavia.</p>
        ) : (
          <ul>
            {state.yourQueuedActions.map((action, index) => (
              <li key={index}>{action.cardId}</li>
            ))}
          </ul>
        )}
      </section>

      <section>
        <h2>Registro</h2>
        <ul>
          {state.events.map((event, index) => (
            <li key={index}>
              Ronda {event.roundNumber}: {event.description}
            </li>
          ))}
        </ul>
      </section>
    </>
  );
}
