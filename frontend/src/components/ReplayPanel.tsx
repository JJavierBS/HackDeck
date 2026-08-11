import { useEffect, useMemo, useState } from "react";
import type { CardDto, HistoryEventDto, MatchHistoryDto, RestClient } from "../api/restClient";
import type { GameSession } from "../api/session";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";

interface ReplayPanelProps {
  client: RestClient;
  session: GameSession;
  cards: CardDto[];
}

interface Step {
  halfNumber: number;
  roundNumber: number;
  events: HistoryEventDto[];
  ciaAfter: Record<string, number> | null;
}

/**
 * Rebobina el match ronda a ronda para comentarlo en clase. Se apoya en la
 * foto de la triada que deja cada cierre de ronda, asi que no hace falta
 * volver a pasar las reglas.
 */
export function ReplayPanel({ client, session, cards }: ReplayPanelProps) {
  const { t, fromServer } = useLanguage();
  const [history, setHistory] = useState<MatchHistoryDto | null>(null);
  const [index, setIndex] = useState(0);

  useEffect(() => {
    client
      .getHistory(session)
      .then(setHistory)
      .catch(() => {});
  }, [client, session]);

  const steps = useMemo<Step[]>(() => {
    if (history === null) {
      return [];
    }
    const byRound = new Map<string, Step>();
    for (const event of history.events) {
      const key = `${event.halfNumber}-${event.roundNumber}`;
      const step = byRound.get(key) ?? {
        halfNumber: event.halfNumber,
        roundNumber: event.roundNumber,
        events: [],
        ciaAfter: null,
      };
      step.events.push(event);
      if (event.type === "ROUND_RESOLVED") {
        step.ciaAfter = event.ciaAfter;
      }
      byRound.set(key, step);
    }
    return [...byRound.values()];
  }, [history]);

  if (history === null || steps.length === 0) {
    return null;
  }
  const step = steps[Math.min(index, steps.length - 1)];
  /**
   * Los ataques y defensas ya nombran su carta en el texto; el twist no, que
   * solo dice que el instructor lanzo algo.
   */
  const cardSuffix = (event: HistoryEventDto) => {
    if (event.type !== "TWIST_LAUNCHED") {
      return null;
    }
    const card = cards.find((candidate) => candidate.id === event.cardId);
    return card === undefined ? null : fromServer(card.name);
  };

  return (
    <section>
      <h2>{t("replay.title")}</h2>
      <p className="tenue">{t("replay.hint")}</p>
      <p className="marca-valor">
        {step.halfNumber === 0
          ? t("lobby.teams")
          : `${t("game.half")} ${step.halfNumber} · ${t("game.round")} ${step.roundNumber}`}{" "}
        <span className="tenue">
          ({index + 1}/{steps.length})
        </span>
      </p>

      <ul>
        {step.events
          .filter((event) => event.type !== "ROUND_RESOLVED")
          .map((event, position) => (
            <li
              key={position}
              className={`evento ${
                event.actor === "ATTACKER" ? "de-atacante" : event.actor === "DEFENDER" ? "de-defensor" : ""
              }`}
            >
              {event.description}
              {cardSuffix(event) !== null && <span className="tenue"> · {cardSuffix(event)}</span>}
            </li>
          ))}
      </ul>

      {step.ciaAfter !== null && (
        <p>
          <span className="marca-etiqueta">{t("replay.cia")}</span>{" "}
          {Object.entries(step.ciaAfter)
            .map(([pillar, level]) => `${t(`cia.${pillar}` as TranslationKey)} ${level}`)
            .join(" · ")}
        </p>
      )}

      <div className="etiquetas">
        <button disabled={index === 0} onClick={() => setIndex(index - 1)}>
          {t("replay.previous")}
        </button>
        <button disabled={index >= steps.length - 1} onClick={() => setIndex(index + 1)}>
          {t("replay.next")}
        </button>
      </div>
    </section>
  );
}
