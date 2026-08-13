import { useEffect, useRef, useState } from "react";
import type { GameStateDto, RestClient } from "../api/restClient";
import type { GameSession } from "../api/session";
import { useCatalog } from "../api/useCatalog";
import { ActiveLayers } from "../components/ActiveLayers";
import { ErrorAlert } from "../components/ErrorAlert";
import { RivalDefences } from "../components/RivalDefences";
import { CardPicker } from "../components/CardPicker";
import { CiaPanel } from "../components/CiaPanel";
import { EventLog } from "../components/EventLog";
import { GameHeader } from "../components/GameHeader";
import { RoundTimer } from "../components/RoundTimer";
import { KillChainPanel } from "../components/KillChainPanel";
import { QueuePanel } from "../components/QueuePanel";
import { RoundReveal } from "../components/RoundReveal";
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
  const [error, setError] = useState<unknown>(null);
  const [pending, setPending] = useState(false);
  const attacking = state.yourSide === "ATTACKER";

  // Al cerrarse una ronda se abre el resumen de la que acaba de pasar.
  const [revealedRound, setRevealedRound] = useState<number | null>(null);
  const lastRound = useRef(state.currentRoundNumber);
  useEffect(() => {
    if (state.currentRoundNumber > lastRound.current) {
      setRevealedRound(lastRound.current);
    }
    lastRound.current = state.currentRoundNumber;
  }, [state.currentRoundNumber]);

  const play = (cardId: string, parameters: Record<string, string>) => {
    setError(null);
    setPending(true);
    client
      .enqueueAction(session, { cardId, parameters })
      .then(onChange)
      .catch((cause: unknown) => {
        setError(cause);
      })
      .finally(() => setPending(false));
  };

  const isReady = state.readyTeams.includes(state.yourTeam ?? "");

  const handleDequeue = (intentId: string) => {
    setError(null);
    setPending(true);
    client
      .dequeueAction(session, intentId)
      .then(onChange)
      .catch((cause: unknown) => setError(cause))
      .finally(() => setPending(false));
  };

  const handleReorder = (intentIds: string[]) => {
    setError(null);
    setPending(true);
    client
      .reorderQueue(session, intentIds)
      .then(onChange)
      .catch((cause: unknown) => setError(cause))
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
              disabled={pending || isReady}
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
            {isReady
              ? state.readyTeams.length > 1
                ? t("ready.done")
                : `${t("ready.done")} · ${t("ready.waiting")}`
              : t("queue.empty")}
          </p>
        </section>
      )}
      {state.result !== null && <MatchScoreboard state={state} />}
      {revealedRound !== null && state.yourSide !== null && (
        <RoundReveal
          events={state.events.filter((event) => event.roundNumber === revealedRound)}
          cards={cards}
          mySide={state.yourSide}
          onClose={() => setRevealedRound(null)}
        />
      )}
      <ErrorAlert error={error} />

      <div className="columnas">
        <div>
          {state.phase === "IN_PROGRESS" && (
            <CardPicker cards={cards} state={state} onPlay={play} pending={pending} />
          )}
        </div>
        <div>
          <CiaPanel state={state} />
          {attacking && <KillChainPanel unlocked={state.yourKillChain} />}
          <QueuePanel
            state={state}
            cards={cards}
            disabled={pending || isReady || state.phase !== "IN_PROGRESS"}
            onRemove={handleDequeue}
            onReorder={handleReorder}
          />
          <ActiveLayers state={state} />
          {attacking && <RivalDefences state={state} />}
          <EventLog events={state.events} hint={!attacking} />
        </div>
      </div>
    </main>
  );
}
