import type { CardDto, GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";

export function ActiveLayers({ state, cards }: { state: GameStateDto; cards: CardDto[] }) {
  const { t, fromServer } = useLanguage();
  const nameOf = (cardId: string) => {
    const card = cards.find((candidate) => candidate.id === cardId);
    return card === undefined ? cardId : fromServer(card.name);
  };

  return (
    <section>
      <h2>{t("layers.title")}</h2>
      {state.yourActiveCards.length === 0 ? (
        <p className="tenue">{t("layers.empty")}</p>
      ) : (
        <ul>
          {state.yourActiveCards.map((layer, index) => (
            <li key={index}>
              · {nameOf(layer.cardId)}{" "}
              <span className="tenue">
                (
                {layer.roundsRemaining === null
                  ? t("layers.permanent")
                  : `${layer.roundsRemaining} ${t("layers.rounds")}`}
                )
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
