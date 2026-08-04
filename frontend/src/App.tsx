import { useMemo, useState } from "react";
import { createRestClient, type RestClient } from "./api/restClient";
import { clearSession, loadSession, saveSession, type GameSession } from "./api/session";
import { useGameState } from "./api/useGameState";
import { EntryView } from "./views/EntryView";
import { InstructorView } from "./views/InstructorView";
import { AttackerView } from "./views/AttackerView";
import { DefenderView } from "./views/DefenderView";

export function App() {
  const client = useMemo(() => createRestClient(), []);
  const [session, setSession] = useState<GameSession | null>(() => loadSession());

  if (session === null) {
    return (
      <EntryView
        client={client}
        onSession={(newSession) => {
          saveSession(newSession);
          setSession(newSession);
        }}
      />
    );
  }

  return (
    <>
      <GameRouter client={client} session={session} />
      <button
        onClick={() => {
          clearSession();
          setSession(null);
        }}
      >
        Salir de la partida
      </button>
    </>
  );
}

interface GameRouterProps {
  client: RestClient;
  session: GameSession;
}

/**
 * Unico punto que se suscribe al estado: las vistas lo reciben ya resuelto.
 * El bando tampoco lo decide el cliente, viene del servidor, que es quien
 * sabe que mitad se esta jugando.
 */
function GameRouter({ client, session }: GameRouterProps) {
  const state = useGameState(client, session);

  if (session.team === null) {
    return <InstructorView client={client} session={session} state={state} />;
  }
  return state?.yourSide === "DEFENDER" ? (
    <DefenderView session={session} state={state} />
  ) : (
    <AttackerView session={session} state={state} />
  );
}
