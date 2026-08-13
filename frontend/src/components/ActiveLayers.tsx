import type { GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";

export function ActiveLayers({ state }: { state: GameStateDto }) {
  const { t, fromServer } = useLanguage();

  return (
    <section>
      <h2>{t("layers.title")}</h2>
      {state.yourActiveCards.length === 0 ? (
        <p className="tenue">{t("layers.empty")}</p>
      ) : (
        <ul>
          {state.yourActiveCards.map((layer, index) => (
            <li key={index}>
              · {layer.cardName === null ? layer.cardId : fromServer(layer.cardName)}{" "}
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
