import type { GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";

const STATUS_WIDTH: Record<string, number> = { INTACT: 100, DAMAGED: 65, CRITICAL: 30, DOWN: 0 };

/**
 * El defensor ve el valor exacto y el atacante solo una lectura
 * cualitativa, asi que el mismo panel pinta lo que haya llegado.
 */
export function CiaPanel({ state }: { state: GameStateDto }) {
  const { t } = useLanguage();
  const pillars = state.ciaLevels ?? state.ciaStatus;
  if (pillars === null) {
    return null;
  }

  return (
    <section>
      <h2>{t("cia.title")}</h2>
      {Object.entries(pillars).map(([pillar, value]) => {
        const exact = typeof value === "number";
        const level = exact ? value : STATUS_WIDTH[value];
        return (
          <div className="pilar" key={pillar}>
            <div className="pilar-cabecera">
              <span>{t(`cia.${pillar}` as TranslationKey)}</span>
              <span>{exact ? value : t(`cia.status.${value}` as TranslationKey)}</span>
            </div>
            <div className="pilar-barra">
              <div
                className={`pilar-relleno ${level < 40 ? "critico" : level < 80 ? "tocado" : ""}`}
                style={{ width: `${level}%` }}
              />
            </div>
          </div>
        );
      })}
    </section>
  );
}
