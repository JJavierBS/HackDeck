import { useState } from "react";
import { ApiRequestError, type RestClient } from "../api/restClient";
import type { GameSession } from "../api/session";

interface EntryViewProps {
  client: RestClient;
  onSession: (session: GameSession) => void;
}

export function EntryView({ client, onSession }: EntryViewProps) {
  const [code, setCode] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [instructorKey, setInstructorKey] = useState("");
  const [error, setError] = useState<string | null>(null);

  const run = (action: Promise<GameSession>) => {
    setError(null);
    action.then(onSession).catch((cause: unknown) => {
      setError(cause instanceof ApiRequestError ? cause.message : "No se ha podido contactar con el servidor");
    });
  };

  return (
    <main>
      <h1>Cyber Range</h1>

      <section>
        <h2>Soy el instructor</h2>
        <input
          value={instructorKey}
          onChange={(event) => setInstructorKey(event.target.value)}
          placeholder="Clave de instructor (solo en remoto)"
        />
        <button onClick={() => run(client.createGame(instructorKey || undefined))}>Crear partida</button>
      </section>

      <section>
        <h2>Somos un equipo</h2>
        <input
          value={code}
          onChange={(event) => setCode(event.target.value.toUpperCase())}
          placeholder="Codigo de partida"
          maxLength={6}
        />
        <input
          value={displayName}
          onChange={(event) => setDisplayName(event.target.value)}
          placeholder="Nombre del equipo"
          maxLength={24}
        />
        <button disabled={!code || !displayName} onClick={() => run(client.joinGame(code, displayName))}>
          Unirse
        </button>
      </section>

      {error && <p role="alert">{error}</p>}
    </main>
  );
}
