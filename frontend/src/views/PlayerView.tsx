import { useState } from "react";
import { ApiRequestError, type GameStateDto, type RestClient } from "../api/restClient";
import type { GameSession } from "../api/session";
import { useCatalog } from "../api/useCatalog";
import { ActiveLayers } from "../components/ActiveLayers";
import { CardPicker } from "../components/CardPicker";
import { CiaPanel } from "../components/CiaPanel";
import { EventLog } from "../components/EventLog";
import { GameHeader } from "../components/GameHeader";
import { RoundTimer } from "../components/RoundTimer";
import { KillChainPanel } from "../components/KillChainPanel";
import { QueuePanel } from "../components/QueuePanel";
import { useLanguage } from "../i18n/LanguageContext";
import { MatchScoreboard } from "./MatchScoreboard";

interface PlayerViewProps {
  client: RestClient;
  session: GameSession;
  state: GameStateDto;
  onChange: () => void;
}

/**
 * Atacante y defensor comparten pantalla: lo que cambia es el catalogo que
 * reciben y los paneles que el servidor les deja ver.
 */
export function PlayerView({ client, session, state, onChange }: PlayerViewProps) {
  const { t } = useLanguage();
  const cards = useCatalog(client, session, state.halfNumber);
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);
  const attacking = state.yourSide === "ATTACKER";

  const play = (cardId: string) => {
    setError(null);
    setPending(true);
    client
      .enqueueAction(session, { cardId, parameters: {} })
      .then(onChange)
      .catch((cause: unknown) => {
        setError(cause instanceof ApiRequestError ? cause.message : t("entry.error.network"));
      })
      .finally(() => setPending(false));
  };

  return (
    <main className={attacking ? "rol-atacante" : "rol-defensor"}>
      <GameHeader title={attacking ? t("role.attacker") : t("role.defender")} state={state} />
      {state.phase === "IN_PROGRESS" && (
        <section>
          <div className="barra-superior">
            <RoundTimer deadlineAt={state.roundDeadlineAt} />
            <button
              disabled={pending || state.readyTeams.includes(state.yourTeam ?? "")}
              onClick={() => {
                setPending(true);
                client
                  .markReady(session)
                  .then(onChange)
                  .catch(() => {})
                  .finally(() => setPending(false));
              }}
            >
              {t("ready.button")}
            </button>
          </div>
          <p className="tenue">
            {state.readyTeams.includes(state.yourTeam ?? "")
              ? state.readyTeams.length > 1
                ? t("ready.done")
                : `${t("ready.done")} · ${t("ready.waiting")}`
              : t("queue.empty")}
          </p>
        </section>
      )}
      {state.result !== null && <MatchScoreboard state={state} />}
      {error !== null && <p className="error">{error}</p>}

      <div className="columnas">
        <div>
          {state.phase === "IN_PROGRESS" && (
            <CardPicker cards={cards} state={state} onPlay={play} pending={pending} />
          )}
        </div>
        <div>
          <CiaPanel state={state} />
          {attacking && <KillChainPanel unlocked={state.yourKillChain} />}
          <QueuePanel state={state} cards={cards} />
          <ActiveLayers state={state} cards={cards} />
          <EventLog events={state.events} hint={!attacking} />
        </div>
      </div>
    </main>
  );
}
