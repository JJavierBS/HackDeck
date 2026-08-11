import type { CardDto, GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";

export function QueuePanel({ state, cards }: { state: GameStateDto; cards: CardDto[] }) {
  const { t, fromServer } = useLanguage();
  const nameOf = (cardId: string) => {
    const card = cards.find((candidate) => candidate.id === cardId);
    return card === undefined ? cardId : fromServer(card.name);
  };

  return (
    <section>
      <h2>{t("queue.title")}</h2>
      {state.yourQueuedActions.length === 0 ? (
        <p className="tenue">{t("queue.empty")}</p>
      ) : (
        <ul>
          {state.yourQueuedActions.map((action, index) => (
            <li key={index}>· {nameOf(action.cardId)}</li>
          ))}
        </ul>
      )}
    </section>
  );
}
