import type { CardDto, GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";
import { CardTile } from "./CardTile";

interface CardPickerProps {
  cards: CardDto[];
  state: GameStateDto;
  onPlay: (cardId: string, parameters: Record<string, string>) => void;
  pending: boolean;
}

const ATTACK_GROUPS = ["RECON", "ACCESS", "ESCALATION", "IMPACT"] as const;
const DEFENCE_GROUPS = ["HYGIENE", "ARCHITECTURE", "DETECTION", "RESPONSE"] as const;

/** Las cartas de accion. Los power-ups van aparte, en PowerUpPicker. */
export function CardPicker({ cards, state, onPlay, pending }: CardPickerProps) {
  const { t } = useLanguage();
  const attacking = state.yourSide === "ATTACKER";
  const groups: readonly string[] = attacking ? ATTACK_GROUPS : DEFENCE_GROUPS;

  const groupOf = (card: CardDto) => (attacking ? card.phase : card.category);

  return (
    <>
      {groups.map((group) => {
        const inGroup = cards.filter((card) => card.type !== "POWERUP" && groupOf(card) === group);
        if (inGroup.length === 0) {
          return null;
        }
        return (
          <section key={group}>
            <h2>
              {attacking
                ? t(`killchain.${group}` as TranslationKey)
                : t(`category.${group}` as TranslationKey)}
            </h2>
            {inGroup.map((card) => (
              <CardTile key={card.id} card={card} state={state} onPlay={onPlay} pending={pending} />
            ))}
          </section>
        );
      })}
    </>
  );
}
