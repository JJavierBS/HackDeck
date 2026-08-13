import type { GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";

export function GameHeader({ title, state }: { title: string; state: GameStateDto }) {
  const { t } = useLanguage();

  return (
    <header className="barra-superior">
      <div className="barra-superior-brand">
        <img src="/hackdecklogo.png" alt="HackDeck Logo" className="barra-superior-logo" />
        <h1 style={{ margin: 0 }}>{title}</h1>
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
          <div className="marca-etiqueta">{t("game.budget")}</div>
          <div className="marca-valor">{state.yourBudget ?? "-"}</div>
        </div>
      </div>
    </header>
  );
}
