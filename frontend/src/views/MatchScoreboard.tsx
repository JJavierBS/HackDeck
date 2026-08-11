import type { GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";

export function MatchScoreboard({ state }: { state: GameStateDto }) {
  const { t } = useLanguage();
  const result = state.result;
  if (result === null) {
    return null;
  }
  const winnerName = result.winner === null ? null : state.teams[result.winner];

  return (
    <section>
      <h2>{t("result.title")}</h2>
      <p className="marca-valor">
        {winnerName === null ? t("result.draw") : `${t("result.winner")} ${winnerName}`}{" "}
        <span className="tenue">
          {t("result.by")} {t(`result.${result.outcome}` as TranslationKey)}
        </span>
      </p>
      <ul>
        {Object.entries(result.defendedCia).map(([team, defended]) => (
          <li key={team}>
            {state.teams[team]} {t("result.defended")} {defended} {t("result.points")}
            {result.takedownRound[team] !== undefined
              ? `, ${t("result.takedownRound")} ${result.takedownRound[team]}`
              : ""}
          </li>
        ))}
      </ul>
    </section>
  );
}
