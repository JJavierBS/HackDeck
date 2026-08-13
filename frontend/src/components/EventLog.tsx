import type { GameEventDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";

export function EventLog({ events, hint }: { events: GameEventDto[]; hint?: boolean }) {
  const { t, fromServer } = useLanguage();
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
              <div>
                {event.cardName === null ? event.description : fromServer(event.cardName)}
                {event.detail !== null && event.detail.success !== null && (
                  <span className={event.detail.success ? "acierto" : "error"}>
                    {" "}
                    {event.detail.success ? t("reveal.success") : t("reveal.failed")}
                  </span>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
