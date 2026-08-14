import type { CardDto, GameStateDto } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";

interface QueuePanelProps {
  state: GameStateDto;
  cards: CardDto[];
  disabled?: boolean;
  onRemove?: (intentId: string) => void;
  onReorder?: (intentIds: string[]) => void;
}

export function QueuePanel({ state, cards, disabled = false, onRemove, onReorder }: QueuePanelProps) {
  const { t, fromServer } = useLanguage();
  const nameOf = (cardId: string) => {
    const card = cards.find((candidate) => candidate.id === cardId);
    return card === undefined ? cardId : fromServer(card.name);
  };

  const actions = state.yourQueuedActions;

  const moveUp = (index: number) => {
    if (index <= 0 || !onReorder) return;
    const ids = actions.map((a) => a.intentId);
    [ids[index - 1], ids[index]] = [ids[index], ids[index - 1]];
    onReorder(ids);
  };

  const moveDown = (index: number) => {
    if (index >= actions.length - 1 || !onReorder) return;
    const ids = actions.map((a) => a.intentId);
    [ids[index], ids[index + 1]] = [ids[index + 1], ids[index]];
    onReorder(ids);
  };

  return (
    <section>
      <h2>{t("queue.title")}</h2>
      {actions.length === 0 ? (
        <p className="tenue">{t("queue.empty")}</p>
      ) : (
        <ul className="lista-cola">
          {actions.map((action, index) => (
            <li key={action.intentId || index} className="item-cola">
              <span className="item-cola-nombre">
                <span className="item-cola-indice">{index + 1}.</span> {nameOf(action.cardId)}
              </span>
              {!disabled && (
                <div className="item-cola-acciones">
                  <button
                    type="button"
                    className="btn-icono-mono"
                    disabled={index === 0}
                    onClick={() => moveUp(index)}
                    title={t("queue.moveUp")}
                  >
                    ▲
                  </button>
                  <button
                    type="button"
                    className="btn-icono-mono"
                    disabled={index === actions.length - 1}
                    onClick={() => moveDown(index)}
                    title={t("queue.moveDown")}
                  >
                    ▼
                  </button>
                  <button
                    type="button"
                    className="btn-icono-mono btn-peligro-mono"
                    onClick={() => onRemove?.(action.intentId)}
                    title={t("queue.remove")}
                  >
                    x
                  </button>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
