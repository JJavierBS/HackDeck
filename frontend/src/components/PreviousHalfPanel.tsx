import type { GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";

/**
 * Al cambiar de bando la triada vuelve a 100, asi que sin esto el nuevo
 * defensor no sabria que marca tiene que batir.
 */
export function PreviousHalfPanel({ state }: { state: GameStateDto }) {
  const { t } = useLanguage();
  const previous = state.previousHalf;
  if (previous === null) {
    return null;
  }
  const defender = state.teams[previous.defendingTeam] ?? previous.defendingTeam;

  return (
    <section className="mitad-anterior">
      <h2>
        {t("previousHalf.title")} {previous.number}
      </h2>
      <p className="tenue">
        {defender} {t("result.defended")}
      </p>
      {/* Misma forma que la triada viva, apagada: asi se comparan de un vistazo. */}
      {Object.entries(previous.ciaLevels).map(([pillar, level]) => (
        <div className="pilar" key={pillar}>
          <div className="pilar-cabecera">
            <span>{t(`cia.${pillar}` as TranslationKey)}</span>
            <span>{level}</span>
          </div>
          <div className="pilar-barra">
            <div className="pilar-relleno" style={{ width: `${level}%` }} />
          </div>
        </div>
      ))}
      <p className="marca-valor">
        {previous.defendedCia} <span className="tenue">{t("previousHalf.toBeat")}</span>
      </p>
      {previous.takedownRound !== null && (
        <p className="tenue">
          {t("result.takedownRound")} {previous.takedownRound}
        </p>
      )}
    </section>
  );
}
