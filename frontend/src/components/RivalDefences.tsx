import type { GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";

export function RivalDefences({ state }: { state: GameStateDto }) {
  const { t, fromServer } = useLanguage();
  if (state.revealedRivalCards.length === 0) {
    return null;
  }

  return (
    <section className="resumen">
      <h2>{t("reveal.rivalDefences")}</h2>
      <ul>
        {state.revealedRivalCards.map((carta, index) => (
          <li key={index}>· {carta.cardName === null ? carta.cardId : fromServer(carta.cardName)}</li>
        ))}
      </ul>
    </section>
  );
}
