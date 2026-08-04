import type { GameStateDto } from "../api/restClient";
import type { GameSession } from "../api/session";
import { MatchScoreboard } from "./MatchScoreboard";

interface ViewProps {
  session: GameSession;
  state: GameStateDto | null;
}

export function AttackerView({ session, state }: ViewProps) {
  return (
    <main>
      <h1>Atacante</h1>
      <p>
        Equipo {session.team} — mitad {state?.halfNumber ?? "-"} de 2, ronda {state?.currentRoundNumber ?? 0} de{" "}
        {state?.roundsPerHalf ?? "-"}
      </p>
      <p>Presupuesto: {state?.yourBudget ?? 0}</p>
      {state && <MatchScoreboard state={state} />}
    </main>
  );
}
