import type { GameStateDto } from "../api/restClient";
import type { GameSession } from "../api/session";

interface ViewProps {
  session: GameSession;
  state: GameStateDto | null;
}

export function AttackerView({ session, state }: ViewProps) {
  return (
    <main>
      <h1>Atacante</h1>
      <p>
        Equipo {session.team}. Fase: {state?.phase ?? "cargando"}
      </p>
      <p>Ronda: {state?.currentRoundNumber ?? 0}</p>
    </main>
  );
}
