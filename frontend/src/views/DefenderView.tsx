import type { GameStateDto } from "../api/restClient";
import type { GameSession } from "../api/session";
import { GameFeed } from "./GameFeed";
import { MatchScoreboard } from "./MatchScoreboard";

interface ViewProps {
  session: GameSession;
  state: GameStateDto | null;
}

export function DefenderView({ session, state }: ViewProps) {
  return (
    <main>
      <h1>Defensor</h1>
      <p>
        Equipo {session.team} — mitad {state?.halfNumber ?? "-"} de 2, ronda {state?.currentRoundNumber ?? 0} de{" "}
        {state?.roundsPerHalf ?? "-"}
      </p>
      <p>Presupuesto: {state?.yourBudget ?? 0}</p>
      {state && <GameFeed state={state} />}
      {state && <MatchScoreboard state={state} />}
    </main>
  );
}
