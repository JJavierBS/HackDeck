import { useEffect, useMemo, useState } from "react";
import { createRestClient, type RestClient } from "./api/restClient";
import {
  clearSession,
  clearTournament,
  loadSession,
  loadTournament,
  saveSession,
  saveTournament,
  type GameSession,
  type TournamentSession,
} from "./api/session";
import { useGameState } from "./api/useGameState";
import { useLanguage } from "./i18n/LanguageContext";
import { LandingView } from "./views/LandingView";
import { TournamentView } from "./views/TournamentView";
import { TournamentPlayerView } from "./views/TournamentPlayerView";
import { TournamentProjectionView } from "./views/TournamentProjectionView";
import { ProjectionView } from "./views/ProjectionView";
import { InstructorView } from "./views/InstructorView";
import { PlayerView } from "./views/PlayerView";

export function App() {
  const { t, toggle } = useLanguage();
  const client = useMemo(() => createRestClient(), []);
  const [session, setSession] = useState<GameSession | null>(() => loadSession());
  const [tournament, setTournament] = useState<TournamentSession | null>(() => loadTournament());
  const projected = useMemo(() => projectionSession(), []);
  const projectedTournament = useMemo(() => tournamentProjection(), []);

  if (projectedTournament !== null) {
    return <TournamentProjectionView client={client} session={projectedTournament} />;
  }

  // La proyeccion vive en su propia URL para que el instructor pueda
  // consultar su panel sin que lo lea toda la clase.
  if (projected !== null) {
    return <ProjectionView session={projected} />;
  }

  return (
    <>
      <nav className="barra-superior">
        <button onClick={toggle}>{t("app.language")}</button>
        {(session !== null || tournament !== null) && (
          <button
            onClick={() => {
              clearSession();
              clearTournament();
              setSession(null);
              setTournament(null);
            }}
          >
            {t("app.leave")}
          </button>
        )}
      </nav>

      {(() => {
        // Un equipo de torneo salta de mesa solo: mientras no tenga, espera.
        if (tournament !== null && !tournament.instructor && session === null) {
          return (
            <TournamentPlayerView
              client={client}
              session={tournament}
              onTable={(mesa) => {
                saveSession(mesa);
                setSession(mesa);
              }}
            />
          );
        }
        if (tournament !== null && tournament.instructor) {
          return <TournamentView client={client} session={tournament} />;
        }
        if (session === null) {
          return (
            <LandingView
              client={client}
              onSession={(nueva) => {
                saveSession(nueva);
                setSession(nueva);
              }}
              onTournament={(nuevo) => {
                saveTournament(nuevo);
                setTournament(nuevo);
              }}
            />
          );
        }
        return (
          <GameRouter
            client={client}
            session={session}
            onLeaveTable={
              tournament === null
                ? undefined
                : () => {
                    clearSession();
                    setSession(null);
                  }
            }
          />
        );
      })()}
    </>
  );
}

function tournamentProjection(): TournamentSession | null {
  const params = new URLSearchParams(window.location.search);
  const tournamentId = params.get("torneo");
  const token = params.get("token");
  if (tournamentId === null || token === null) {
    return null;
  }
  return { tournamentId, joinCode: "", token, instructor: true };
}

function projectionSession(): GameSession | null {
  const params = new URLSearchParams(window.location.search);
  const gameId = params.get("proyeccion");
  const token = params.get("token");
  if (gameId === null || token === null) {
    return null;
  }
  return { gameId, joinCode: "", team: null, token };
}

/** Unico punto que se suscribe al estado; las vistas lo reciben resuelto. */
function GameRouter({
  client,
  session,
  onLeaveTable,
}: {
  client: RestClient;
  session: GameSession;
  onLeaveTable?: () => void;
}) {
  const { t } = useLanguage();
  const { state, refresh } = useGameState(client, session);

  // En torneo, al acabar la mesa se vuelve a la espera y el siguiente
  // emparejamiento entra solo.
  useEffect(() => {
    if (onLeaveTable !== undefined && state !== null && state.phase === "FINISHED") {
      const salida = setTimeout(onLeaveTable, 8000);
      return () => clearTimeout(salida);
    }
    return undefined;
  }, [onLeaveTable, state]);

  if (state === null) {
    return (
      <main>
        <p className="tenue">{t("app.loading")}</p>
      </main>
    );
  }
  if (session.team === null) {
    return <InstructorView client={client} session={session} state={state} onChange={refresh} />;
  }
  if (state.phase === "PREPARATION") {
    return (
      <main>
        <h1>{t("app.title")}</h1>
        <section>
          <p>{t("game.waiting")}</p>
          <p className="tenue">
            {t("game.code")}: {session.joinCode}
          </p>
        </section>
      </main>
    );
  }
  return <PlayerView client={client} session={session} state={state} onChange={refresh} />;
}
