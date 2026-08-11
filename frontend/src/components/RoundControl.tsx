import type { CardDto, GameStateDto, RestClient } from "../api/restClient";
import type { GameSession } from "../api/session";
import { useLanguage } from "../i18n/LanguageContext";
import { RoundTimer } from "./RoundTimer";

interface RoundControlProps {
  client: RestClient;
  session: GameSession;
  state: GameStateDto;
  cards: CardDto[];
  run: (action: Promise<unknown>) => void;
}

/**
 * El mando del instructor. Aqui si se ven las dos colas antes de resolver,
 * porque esta pantalla es la de su portatil: la que se proyecta es otra.
 */
export function RoundControl({ client, session, state, cards, run }: RoundControlProps) {
  const { t, fromServer } = useLanguage();
  const nameOf = (cardId: string) => {
    const card = cards.find((candidate) => candidate.id === cardId);
    return card === undefined ? cardId : fromServer(card.name);
  };

  return (
    <section>
      <h2>{t("control.title")}</h2>

      <div className="barra-superior">
        <RoundTimer deadlineAt={state.roundDeadlineAt} />
        <div>
          <div className="marca-etiqueta">{t("ready.teams")}</div>
          <div className="marca-valor">
            {state.readyTeams.length === 0
              ? "—"
              : state.readyTeams.map((team) => state.teams[team] ?? team).join(" · ")}
          </div>
        </div>
      </div>

      <div className="etiquetas">
        <button onClick={() => run(client.setAutoResolve(session, !state.autoResolve))}>
          {state.autoResolve ? t("control.autoOn") : t("control.autoOff")}
        </button>
        <button onClick={() => run(client.resolveRound(session))}>{t("lobby.resolve")}</button>
        <button onClick={() => run(client.closeHalf(session))}>{t("control.closeHalf")}</button>
        <button onClick={() => run(client.closeMatch(session))}>{t("control.closeMatch")}</button>
      </div>
      <p className="tenue">{t("control.autoHint")}</p>

      {state.queuedBySide !== null && (
        <>
          <h2>{t("control.queues")}</h2>
          <p className="tenue">{t("control.queuesHint")}</p>
          <ul>
            {Object.entries(state.queuedBySide).map(([side, actions]) => (
              <li key={side} className={`evento ${side === "ATTACKER" ? "de-atacante" : "de-defensor"}`}>
                {side === "ATTACKER" ? t("role.attacker") : t("role.defender")}:{" "}
                {actions.length === 0 ? "—" : actions.map((action) => nameOf(action.cardId)).join(" · ")}
              </li>
            ))}
          </ul>
        </>
      )}
    </section>
  );
}
