import { useState } from "react";
import type { CardDto, GameEventDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";

interface RoundRevealProps {
  events: GameEventDto[];
  cards: CardDto[];
  mySide: "ATTACKER" | "DEFENDER";
  onClose: () => void;
}

/**
 * Cuenta accion por accion que ha pasado al resolverse la ronda. Sin esto el
 * equipo veia cambiar unos numeros y no sabia por que: si su ataque fallo
 * por saltarse la kill chain, si una defensa lo freno o si simplemente tuvo
 * mala suerte.
 */
export function RoundReveal({ events, cards, mySide, onClose }: RoundRevealProps) {
  const { t, fromServer } = useLanguage();
  const [index, setIndex] = useState(0);

  const steps = events.filter((event) => event.detail !== null);
  if (steps.length === 0) {
    return null;
  }
  const step = steps[Math.min(index, steps.length - 1)];
  const detail = step.detail!;
  const mine = step.actor === mySide;

  const nameOf = (cardId: string | null) => {
    const card = cards.find((candidate) => candidate.id === cardId);
    return card === undefined ? null : fromServer(card.name);
  };
  const titleOf = (event: GameEventDto) =>
    event.cardName !== null ? fromServer(event.cardName) : (nameOf(event.cardId) ?? event.description);
  const pillar = (key: string) => t(`cia.${key}` as TranslationKey);

  return (
    <section className="resumen">
      <h2>{t("reveal.title")}</h2>
      <p className="marca-etiqueta">
        {index + 1} / {steps.length}
      </p>

      <p className="marca-valor">
        {mine || step.type === "DEFENCE" ? (
          <>
            {titleOf(step)}{" "}
            <span className={detail.success === false ? "error" : "acierto"}>
              {step.type === "DEFENCE"
                ? t("reveal.deployed")
                : detail.success
                  ? t("reveal.success")
                  : t("reveal.failed")}
            </span>
          </>
        ) : (
          <>
            <span className="marca-etiqueta">{t("reveal.detectedAttack")}</span> {titleOf(step)}
          </>
        )}
      </p>

      <ul>
        {/* El motivo esta escrito desde el punto de vista de quien la jugo;
            a quien la sufre se le dice si la freno, mas abajo. */}
        {mine && detail.failureReason !== null && (
          <li>{t(`reveal.reason.${detail.failureReason}` as TranslationKey)}</li>
        )}

        {Object.entries(detail.impact).map(([key, value]) => (
          <li key={key}>
            {value < 0 ? t("reveal.damage") : t("reveal.repaired")}: {Math.abs(value)} {pillar(key)}
          </li>
        ))}

        {detail.mitigated > 0 && <li>{t("reveal.mitigated")}</li>}

        {detail.unlocked.length > 0 && (
          <li>
            {t("reveal.unlocked")}:{" "}
            {detail.unlocked.map((phase) => t(`killchain.${phase}` as TranslationKey)).join(" · ")}
          </li>
        )}

        {detail.boosts.length > 0 && (
          <li>
            {t("reveal.boosts")}: {detail.boosts.map((id) => nameOf(id) ?? id).join(" · ")}
          </li>
        )}

        {/* Al atacante se le dice si le han visto; al defensor, que ataque
            detecto y si alguna de sus capas lo freno. */}
        {mine && detail.detected !== null && (
          <li>{detail.detected ? t("reveal.detected") : t("reveal.undetected")}</li>
        )}
        {!mine && step.type === "ATTACK" && (
          <li>
            {detail.counteredBy === null
              ? t("reveal.blockedNothing")
              : `${t("reveal.blockedBy")} ${nameOf(detail.counteredBy) ?? detail.counteredBy}`}
          </li>
        )}
      </ul>

      <div className="etiquetas">
        <button disabled={index === 0} onClick={() => setIndex(index - 1)}>
          {t("reveal.previous")}
        </button>
        {index < steps.length - 1 ? (
          <button onClick={() => setIndex(index + 1)}>{t("reveal.next")}</button>
        ) : (
          <button onClick={onClose}>{t("reveal.close")}</button>
        )}
      </div>
    </section>
  );
}
