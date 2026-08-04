import type { GameStateDto, RestClient } from "../api/restClient";
import type { GameSession } from "../api/session";
import { MatchScoreboard } from "./MatchScoreboard";

interface ViewProps {
  client: RestClient;
  session: GameSession;
  state: GameStateDto | null;
}

export function InstructorView({ client, session, state }: ViewProps) {
  const teams = Object.entries(state?.teams ?? {});

  return (
    <main>
      <h1>Instructor</h1>
      <p>
        Codigo de partida: <strong>{session.joinCode}</strong>
      </p>
      <p>
        Fase: {state?.phase ?? "cargando"}
        {state?.halfNumber !== null && state !== null
          ? ` — mitad ${state.halfNumber} de 2, ronda ${state.currentRoundNumber} de ${state.roundsPerHalf}`
          : ""}
      </p>
      <ul>
        {teams.map(([team, name]) => (
          <li key={team}>
            Equipo {team}: {name}
            {state?.budgets ? ` (presupuesto ${state.budgets[team]})` : ""}
          </li>
        ))}
      </ul>
      <button
        onClick={() => client.startGame(session)}
        disabled={state?.phase !== "PREPARATION" || teams.length < 2}
      >
        Empezar partida
      </button>
      <button onClick={() => client.resolveRound(session)} disabled={state?.phase !== "IN_PROGRESS"}>
        Resolver ronda
      </button>
      {state && <MatchScoreboard state={state} />}
    </main>
  );
}
