import { createRestClient, type GameStateDto } from "../api/restClient";
import type { GameSession } from "../api/session";
import { useGameState } from "../api/useGameState";
import { Loading } from "../components/Loading";
import { RoundTimer } from "../components/RoundTimer";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";
import { useMemo } from "react";

/**
 * Lo que ve toda la clase en el proyector. A proposito no lleva registro de
 * eventos ni colas: en la pared se leen los ataques silenciosos que el
 * defensor todavia no ha detectado.
 */
export function ProjectionView({ session }: { session: GameSession }) {
  const { t } = useLanguage();
  const client = useMemo(() => createRestClient(), []);
  const { state } = useGameState(client, session);

  if (state === null) {
    return (
      <main className="proyeccion">
        <Loading big />
      </main>
    );
  }

  return (
    <main className="proyeccion">
      <header className="proyeccion-cabecera">
        <div className="barra-superior-brand">
          <img src="/hackdecklogo.png" alt="HackDeck Logo" style={{ width: 72, height: 72, objectFit: "contain" }} />
          <div>
            <div className="marca-etiqueta">{t("projection.join")}</div>
            <div className="codigo codigo-gigante">{state.joinCode}</div>
          </div>
        </div>
        <div className="marcas">
          <div>
            <div className="marca-etiqueta">{t("game.half")}</div>
            <div className="codigo">{state.halfNumber ?? "-"}/2</div>
          </div>
          <div>
            <div className="marca-etiqueta">{t("game.round")}</div>
            <div className="codigo">
              {state.currentRoundNumber}/{state.roundsPerHalf}
            </div>
          </div>
          <RoundTimer deadlineAt={state.roundDeadlineAt} big />
        </div>
      </header>

      {state.phase === "PREPARATION" ? (
        <p className="proyeccion-aviso">{t("projection.waiting")}</p>
      ) : (
        <Marcador state={state} />
      )}

      {state.result !== null && (
        <section className="proyeccion-aviso proyeccion-resultado">
          {state.result.winner === null
            ? t("result.draw")
            : `${t("result.winner")} ${state.teams[state.result.winner]}`}
        </section>
      )}
    </main>
  );
}

const PILARES_VACIOS: [string, null][] = [
  ["CONFIDENTIALITY", null],
  ["INTEGRITY", null],
  ["AVAILABILITY", null],
];

/**
 * Las dos defensas a la vez, que es lo que se compara para ganar el match:
 * la del que defiende ahora en vivo y la que ya cerro el otro equipo.
 */
function Marcador({ state }: { state: GameStateDto }) {
  const { t } = useLanguage();
  const previous = state.previousHalf;

  return (
    <section className="proyeccion-marcador">
      {Object.entries(state.teams).map(([team, name]) => {
        const defendiendo = team !== state.attackingTeam;
        const cerrada = previous !== null && previous.defendingTeam === team;
        const levels = defendiendo ? state.ciaLevels : cerrada ? previous.ciaLevels : null;
        const total =
          levels === null ? null : Object.values(levels).reduce((suma, nivel) => suma + nivel, 0);

        return (
          <div key={team} className={defendiendo ? "rol-defensor" : "rol-atacante"}>
            <div className="proyeccion-marcador-cabecera">
              <div>
                <div className="marca-etiqueta">
                  {defendiendo ? t("projection.defendingNow") : t("projection.attackingNow")}
                </div>
                <div className="marca-valor">{name}</div>
              </div>
              {total !== null && (
                <div className="proyeccion-total">
                  <span className="codigo">{total}</span>
                  <span className="marca-etiqueta">{t("projection.defendedTotal")}</span>
                </div>
              )}
            </div>

            {/* En la primera mitad el atacante aun no ha defendido: se dibujan
                los pilares vacios para que las dos columnas midan igual en la
                pared y se vea donde va a aparecer su marca. */}
            <div className={levels === null ? "triada-pendiente" : cerrada ? "triada-cerrada" : undefined}>
              {(levels === null ? PILARES_VACIOS : Object.entries(levels)).map(([pillar, level]) => (
                <div className="pilar" key={pillar}>
                  <div className="pilar-cabecera">
                    <span>{t(`cia.${pillar}` as TranslationKey)}</span>
                    <span className="codigo">{level === null ? "—" : level}</span>
                  </div>
                  <div className="pilar-barra pilar-barra-gorda">
                    {level !== null && (
                      <div
                        className={`pilar-relleno ${level < 40 ? "critico" : level < 80 ? "tocado" : ""}`}
                        style={{ width: `${level}%` }}
                      />
                    )}
                  </div>
                </div>
              ))}
            </div>
            {levels === null && <p className="tenue">{t("projection.notYetDefended")}</p>}

            <div className="marca-etiqueta proyeccion-pie">
              {defendiendo
                ? `${t("previousHalf.title")} ${state.halfNumber}`
                : cerrada
                  ? `${t("previousHalf.title")} ${previous.number} · ${t("projection.closed")}`
                  : ""}
            </div>
          </div>
        );
      })}
    </section>
  );
}
