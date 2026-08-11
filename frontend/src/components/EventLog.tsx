import type { GameEventDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";

export function EventLog({ events, hint }: { events: GameEventDto[]; hint?: boolean }) {
  const { t } = useLanguage();
  const latest = [...events].reverse();

  return (
    <section>
      <h2>{t("log.title")}</h2>
      {hint && <p className="tenue">{t("log.hint")}</p>}
      {latest.length === 0 ? (
        <p className="tenue">{t("log.empty")}</p>
      ) : (
        <ul>
          {latest.map((event, index) => (
            <li
              key={index}
              className={`evento ${event.actor === "ATTACKER" ? "de-atacante" : "de-defensor"}`}
            >
              <span className="tenue">
                {t("game.round")} {event.roundNumber} ·{" "}
                {event.actor === "ATTACKER" ? t("log.attacker") : t("log.defender")}
              </span>
              <div>{event.description}</div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
