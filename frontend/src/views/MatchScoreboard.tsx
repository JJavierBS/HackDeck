import type { GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";

/**
 * El instructor no juega, asi que para el no hay victoria ni derrota: ve
 * quien gano y ya. Para un equipo, en cambio, es el desenlace de la partida.
 */
export function MatchScoreboard({ state }: { state: GameStateDto }) {
  const { t } = useLanguage();
  const result = state.result;
  if (result === null) {
    return null;
  }
  const winnerName = result.winner === null ? null : state.teams[result.winner];
  const playing = state.yourTeam !== null;
  const won = playing && result.winner === state.yourTeam;
  const drew = playing && result.winner === null;
  const desenlace = !playing ? "neutro" : won ? "victoria" : drew ? "empate" : "derrota";

  return (
    <section className={`resultado resultado-${desenlace}`}>
      <h2>{t("result.title")}</h2>
      {playing ? (
        <p className="resultado-titular">
          {won ? t("result.youWin") : drew ? t("result.draw") : t("result.youLose")}
        </p>
      ) : (
        <p className="resultado-titular">
          {winnerName === null ? t("result.draw") : `${t("result.winner")} ${winnerName}`}
        </p>
      )}
      <p className="marca-valor">
        {/* Para el equipo el titular ya dice el desenlace; aqui va el detalle. */}
        {playing && winnerName !== null && !won && `${t("result.winner")} ${winnerName} `}
        {/* "Empate por empate" no lo dice nadie. */}
        {result.outcome !== "DRAW" && (
          <span className="tenue">
            {t("result.by")} {t(`result.${result.outcome}` as TranslationKey)}
          </span>
        )}
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
