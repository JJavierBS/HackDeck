import { createRestClient, type GameStateDto } from "../api/restClient";
import type { GameSession } from "../api/session";
import { useGameState } from "../api/useGameState";
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
        <p className="tenue">{t("app.loading")}</p>
      </main>
    );
  }

  return (
    <main className="proyeccion">
      <header className="proyeccion-cabecera">
        <div>
          <div className="marca-etiqueta">{t("projection.join")}</div>
          <div className="codigo codigo-gigante">{state.joinCode}</div>
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
        <>
          <Equipos state={state} />
          <section className="proyeccion-triada">
            {Object.entries(state.ciaLevels ?? {}).map(([pillar, level]) => (
              <div className="pilar" key={pillar}>
                <div className="pilar-cabecera">
                  <span>{t(`cia.${pillar}` as TranslationKey)}</span>
                  <span className="codigo">{level}</span>
                </div>
                <div className="pilar-barra pilar-barra-gorda">
                  <div
                    className={`pilar-relleno ${level < 40 ? "critico" : level < 80 ? "tocado" : ""}`}
                    style={{ width: `${level}%` }}
                  />
                </div>
              </div>
            ))}
          </section>
        </>
      )}

      {state.result !== null && (
        <section className="proyeccion-aviso">
          {state.result.winner === null
            ? t("result.draw")
            : `${t("result.winner")} ${state.teams[state.result.winner]}`}
        </section>
      )}
    </main>
  );
}

function Equipos({ state }: { state: GameStateDto }) {
  const { t } = useLanguage();
  return (
    <section className="proyeccion-equipos">
      {Object.entries(state.teams).map(([team, name]) => {
        const attacking = team === state.attackingTeam;
        return (
          <div key={team} className={attacking ? "rol-atacante" : "rol-defensor"}>
            <div className="marca-etiqueta">{attacking ? t("role.attacker") : t("role.defender")}</div>
            <div className="marca-valor">{name}</div>
          </div>
        );
      })}
    </section>
  );
}
