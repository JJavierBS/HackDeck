import { useState } from "react";
import { ApiRequestError, type GameStateDto, type RestClient } from "../api/restClient";
import type { GameSession } from "../api/session";
import { useCatalog } from "../api/useCatalog";
import { CiaPanel } from "../components/CiaPanel";
import { EventLog } from "../components/EventLog";
import { useLanguage } from "../i18n/LanguageContext";
import { MatchScoreboard } from "./MatchScoreboard";

interface InstructorViewProps {
  client: RestClient;
  session: GameSession;
  state: GameStateDto;
  onChange: () => void;
}

export function InstructorView({ client, session, state, onChange }: InstructorViewProps) {
  const { t, fromServer } = useLanguage();
  const cards = useCatalog(client, session, state.halfNumber ?? 0);
  const [error, setError] = useState<string | null>(null);
  const teams = Object.entries(state.teams);
  const twists = cards.filter((card) => card.type === "TWIST");

  const run = (action: Promise<unknown>) => {
    setError(null);
    action.then(onChange).catch((cause: unknown) => {
      setError(cause instanceof ApiRequestError ? cause.message : t("entry.error.network"));
    });
  };

  return (
    <main>
      <header className="barra-superior">
        <div>
          <div className="marca-etiqueta">{t("game.code")}</div>
          <div className="codigo">{session.joinCode}</div>
        </div>
        <div className="marcas">
          <div>
            <div className="marca-etiqueta">{t("game.half")}</div>
            <div className="marca-valor">{state.halfNumber ?? "-"} / 2</div>
          </div>
          <div>
            <div className="marca-etiqueta">{t("game.round")}</div>
            <div className="marca-valor">
              {state.currentRoundNumber} / {state.roundsPerHalf}
            </div>
          </div>
          <div>
            <div className="marca-etiqueta">{t("game.timePerRound")}</div>
            <div className="marca-valor">
              {state.roundTimeoutSeconds}
              {t("game.seconds")}
            </div>
          </div>
        </div>
      </header>

      {state.result !== null && <MatchScoreboard state={state} />}
      {error !== null && <p className="error">{error}</p>}

      <div className="columnas">
        <div>
          <section>
            <h2>{t("lobby.teams")}</h2>
            {teams.length === 0 ? (
              <p className="tenue">{t("lobby.empty")}</p>
            ) : (
              <ul>
                {teams.map(([team, name]) => (
                  <li key={team}>
                    <strong>{name}</strong>{" "}
                    <span className="tenue">
                      ({team}
                      {state.budgets === null ? "" : ` · ${t("game.budget")} ${state.budgets[team]}`})
                    </span>
                  </li>
                ))}
              </ul>
            )}
            <div className="etiquetas">
              <button
                disabled={state.phase !== "PREPARATION" || teams.length < 2}
                onClick={() => run(client.startGame(session))}
              >
                {t("lobby.start")}
              </button>
              <button
                disabled={state.phase !== "IN_PROGRESS"}
                onClick={() => run(client.resolveRound(session))}
              >
                {t("lobby.resolve")}
              </button>
            </div>
            {teams.length < 2 && <p className="tenue">{t("lobby.needTeams")}</p>}
          </section>

          {state.phase === "IN_PROGRESS" && twists.length > 0 && (
            <section>
              <h2>{t("lobby.twists")}</h2>
              {twists.map((twist) => (
                <article className="carta" key={twist.id}>
                  <div className="carta-nombre">{fromServer(twist.name)}</div>
                  <p className="carta-descripcion">{fromServer(twist.description)}</p>
                  <button onClick={() => run(client.launchTwist(session, twist.id))}>
                    {t("card.launch")}
                  </button>
                </article>
              ))}
            </section>
          )}
        </div>
        <div>
          <CiaPanel state={state} />
          <EventLog events={state.events} />
        </div>
      </div>
    </main>
  );
}
