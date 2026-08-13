import type { GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";

export function CiaPanel({ state }: { state: GameStateDto }) {
  const { t } = useLanguage();
  if (state.ciaLevels === null) {
    return null;
  }

  return (
    <section>
      <h2>{t("cia.title")}</h2>
      {Object.entries(state.ciaLevels).map(([pillar, level]) => (
        <div className="pilar" key={pillar}>
          <div className="pilar-cabecera">
            <span>{t(`cia.${pillar}` as TranslationKey)}</span>
            <span>{level}</span>
          </div>
          <div className="pilar-barra">
            <div
              className={`pilar-relleno ${level < 40 ? "critico" : level < 80 ? "tocado" : ""}`}
              style={{ width: `${level}%` }}
            />
          </div>
        </div>
      ))}
    </section>
  );
}
