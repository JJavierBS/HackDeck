import type { CardDto, GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";
import { CardTile } from "./CardTile";

interface PowerUpPickerProps {
  cards: CardDto[];
  state: GameStateDto;
  onPlay: (cardId: string, parameters: Record<string, string>) => void;
  pending: boolean;
}

/**
 * Al lado y no al final de la lista de acciones: son las cartas de remontada,
 * y enterradas bajo dieciseis cartas no las encontraba nadie a tiempo.
 */
export function PowerUpPicker({ cards, state, onPlay, pending }: PowerUpPickerProps) {
  const { t } = useLanguage();
  const powerups = cards.filter((card) => card.type === "POWERUP");
  if (powerups.length === 0) {
    return null;
  }

  return (
    <section className="especiales">
      <h2>{t("category.POWERUP")}</h2>
      {powerups.map((card) => (
        <CardTile key={card.id} card={card} state={state} onPlay={onPlay} pending={pending} />
      ))}
    </section>
  );
}
