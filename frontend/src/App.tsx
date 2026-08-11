import { useMemo, useState } from "react";
import { createRestClient, type RestClient } from "./api/restClient";
import { clearSession, loadSession, saveSession, type GameSession } from "./api/session";
import { useGameState } from "./api/useGameState";
import { useLanguage } from "./i18n/LanguageContext";
import { EntryView } from "./views/EntryView";
import { InstructorView } from "./views/InstructorView";
import { PlayerView } from "./views/PlayerView";

export function App() {
  const { t, toggle } = useLanguage();
  const client = useMemo(() => createRestClient(), []);
  const [session, setSession] = useState<GameSession | null>(() => loadSession());

  return (
    <>
      <nav className="barra-superior">
        <button onClick={toggle}>{t("app.language")}</button>
        {session !== null && (
          <button
            onClick={() => {
              clearSession();
              setSession(null);
            }}
          >
            {t("app.leave")}
          </button>
        )}
      </nav>

      {session === null ? (
        <EntryView
          client={client}
          onSession={(newSession) => {
            saveSession(newSession);
            setSession(newSession);
          }}
        />
      ) : (
        <GameRouter client={client} session={session} />
      )}
    </>
  );
}

/**
 * Unico punto que se suscribe al estado: las vistas lo reciben ya resuelto.
 * El bando tampoco lo decide el cliente, viene del servidor, que es quien
 * sabe que mitad se esta jugando.
 */
function GameRouter({ client, session }: { client: RestClient; session: GameSession }) {
  const { t } = useLanguage();
  const { state, refresh } = useGameState(client, session);

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
