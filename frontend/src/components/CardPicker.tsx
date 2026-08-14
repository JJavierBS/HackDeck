import { useState } from "react";
import type { CardDto, GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";

interface CardPickerProps {
  cards: CardDto[];
  state: GameStateDto;
  onPlay: (cardId: string, parameters: Record<string, string>) => void;
  pending: boolean;
}

const ATTACK_GROUPS = ["RECON", "ACCESS", "ESCALATION", "IMPACT"] as const;
const DEFENCE_GROUPS = ["HYGIENE", "ARCHITECTURE", "DETECTION", "RESPONSE"] as const;

export function CardPicker({ cards, state, onPlay, pending }: CardPickerProps) {
  const { t, fromServer } = useLanguage();
  const [anticipado, setAnticipado] = useState("");
  // Solo se puede anticipar un ataque que ya se haya detectado alguna vez.
  const detectados = [
    ...new Map(
      state.events
        .filter((evento) => evento.actor === "ATTACKER" && evento.cardId !== null)
        .map((evento) => [evento.cardId!, evento.cardName]),
    ).entries(),
  ];
  const attacking = state.yourSide === "ATTACKER";
  const budget = state.yourBudget ?? 0;
  const groups: readonly string[] = attacking ? ATTACK_GROUPS : DEFENCE_GROUPS;

  const groupOf = (card: CardDto) =>
    card.type === "POWERUP" ? "POWERUP" : ((attacking ? card.phase : card.category) ?? "POWERUP");

  /**
   * Espeja la regla del servidor: una capa que dura no se refuerza repitiendola,
   * asi que mas vale que el equipo lo vea antes de gastar el clic.
   */
  const alreadyDeployed = (card: CardDto): TranslationKey | null => {
    if (attacking || card.duration === "INSTANT") {
      return null;
    }
    if (state.yourQueuedActions.some((action) => action.cardId === card.id)) {
      return "card.queued";
    }
    const active = state.yourActiveCards.some((layer) => layer.cardId === card.id);
    return card.duration === "PERMANENT" && active ? "card.deployed" : null;
  };

  return (
    <>
      {[...groups, "POWERUP"].map((group) => {
        const inGroup = cards.filter((card) => groupOf(card) === group);
        if (inGroup.length === 0) {
          return null;
        }
        return (
          <section key={group}>
            <h2>
              {group === "POWERUP"
                ? t("category.POWERUP")
                : attacking
                  ? t(`killchain.${group}` as TranslationKey)
                  : t(`category.${group}` as TranslationKey)}
            </h2>
            {inGroup.map((card) => {
              const affordable = card.cost <= budget;
              const phaseLocked =
                attacking && card.phase !== null && !state.yourKillChain.includes(card.phase);
              const deployed = alreadyDeployed(card);
              const blocked = !affordable || pending || phaseLocked || deployed !== null;
              const label = phaseLocked
                ? t("card.phaseLocked")
                : deployed !== null
                  ? t(deployed)
                  : affordable
                    ? t("card.play")
                    : t("card.noBudget");
              return (
                <article className="carta" key={card.id}>
                  <div className="carta-cabecera">
                    <span className="carta-nombre">{fromServer(card.name)}</span>
                    <span className="etiqueta coste">
                      {t("card.cost")} {card.cost}
                    </span>
                  </div>
                  <p className="carta-descripcion">{fromServer(card.description)}</p>
                  <div className="etiquetas">
                    {/* En las defensas el ruido y el acierto son siempre los
                        mismos y solo estorban; en los ataques deciden la
                        jugada, y "ruido nulo" es media carta. */}
                    {attacking && (
                      <>
                        <span className="etiqueta">
                          {t("card.noise")}: {t(`noise.${card.noise}` as TranslationKey)}
                        </span>
                        <span className="etiqueta">
                          {t("card.success")}: {Math.round(card.successRate * 100)}%
                        </span>
                      </>
                    )}
                    {phaseLocked && <span className="etiqueta">{t("card.phaseLocked")}</span>}
                    {deployed !== null && <span className="etiqueta">{t(deployed)}</span>}
                  </div>
                  {card.effects.includes("BLOCKS_CHOSEN_ATTACK") ? (
                    <>
                      <label>
                        {t("block.choose")}
                        <select
                          className="select-cert"
                          value={anticipado}
                          onChange={(evento) => setAnticipado(evento.target.value)}
                          disabled={detectados.length === 0}
                        >
                          <option value="">
                            {detectados.length === 0 ? t("block.none") : "—"}
                          </option>
                          {detectados.map(([id, nombre]) => (
                            <option key={id} value={id}>
                              {nombre === null ? id : fromServer(nombre)}
                            </option>
                          ))}
                        </select>
                      </label>
                      <p className="tenue">{t("block.hint")}</p>
                      <button
                        disabled={blocked || anticipado === ""}
                        onClick={() => onPlay(card.id, { blocks: anticipado })}
                      >
                        {label}
                      </button>
                    </>
                  ) : (
                    <button disabled={blocked} onClick={() => onPlay(card.id, {})}>
                      {label}
                    </button>
                  )}
                </article>
              );
            })}
          </section>
        );
      })}
    </>
  );
}
