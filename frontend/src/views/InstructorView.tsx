import type { GameStateDto, RestClient } from "../api/restClient";
import type { GameSession } from "../api/session";

interface ViewProps {
  client: RestClient;
  session: GameSession;
  state: GameStateDto | null;
}

export function InstructorView({ client, session, state }: ViewProps) {
  return (
    <main>
      <h1>Instructor</h1>
      <p>
        Codigo de partida: <strong>{session.joinCode}</strong>
      </p>
      <p>Fase: {state?.phase ?? "cargando"}</p>
      <p>Ronda: {state?.currentRoundNumber ?? 0}</p>
      <ul>
        {Object.entries(state?.teams ?? {}).map(([team, name]) => (
          <li key={team}>
            Equipo {team}: {name}
          </li>
        ))}
      </ul>
      <button onClick={() => client.startGame(session)} disabled={state?.phase !== "PREPARATION"}>
        Empezar partida
      </button>
      <button onClick={() => client.resolveRound(session)} disabled={state?.phase !== "IN_PROGRESS"}>
        Resolver ronda
      </button>
    </main>
  );
}
