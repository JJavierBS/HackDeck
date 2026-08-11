import type { CardDto, GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";

interface CardPickerProps {
  cards: CardDto[];
  state: GameStateDto;
  onPlay: (cardId: string) => void;
  pending: boolean;
}

const ATTACK_GROUPS = ["RECON", "ACCESS", "ESCALATION", "IMPACT"] as const;
const DEFENCE_GROUPS = ["HYGIENE", "ARCHITECTURE", "DETECTION", "RESPONSE"] as const;

/**
 * Catalogo agrupado por fase de kill chain o por capa defensiva. Cada carta
 * dice lo que cuesta, cuanto ruido hace y que probabilidad tiene, que es lo
 * que hace falta para decidir sin tener que memorizar nada.
 */
export function CardPicker({ cards, state, onPlay, pending }: CardPickerProps) {
  const { t, fromServer } = useLanguage();
  const attacking = state.yourSide === "ATTACKER";
  const budget = state.yourBudget ?? 0;
  const groups: readonly string[] = attacking ? ATTACK_GROUPS : DEFENCE_GROUPS;

  const groupOf = (card: CardDto) =>
    card.type === "POWERUP" ? "POWERUP" : ((attacking ? card.phase : card.category) ?? "POWERUP");

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
                  </div>
                  <button disabled={!affordable || pending} onClick={() => onPlay(card.id)}>
                    {affordable ? t("card.play") : t("card.noBudget")}
                  </button>
                </article>
              );
            })}
          </section>
        );
      })}
    </>
  );
}
