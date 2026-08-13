import { useEffect, useState } from "react";
import type { RestClient, TournamentDto } from "../api/restClient";
import type { GameSession, TournamentSession } from "../api/session";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";

interface TournamentViewProps {
  client: RestClient;
  session: TournamentSession;
}

/**
 * El panel del instructor: todas las mesas de la ronda a la vez. Sin esto no
 * puede llevar seis mesas una sola persona.
 */
export function TournamentView({ client, session }: TournamentViewProps) {
  const { t } = useLanguage();
  const [torneo, setTorneo] = useState<TournamentDto | null>(null);
  const comoPartida: GameSession = {
    gameId: session.tournamentId,
    joinCode: session.joinCode,
    team: null,
    token: session.token,
  };

  useEffect(() => {
    const refrescar = () =>
      client
        .getTournament(comoPartida, session.tournamentId)
        .then(setTorneo)
        .catch(() => {});
    refrescar();
    const reloj = setInterval(refrescar, 2000);
    return () => clearInterval(reloj);
  }, [client, session.tournamentId]);

  if (torneo === null) {
    return (
      <main>
        <p className="tenue">{t("app.loading")}</p>
      </main>
    );
  }

  const accion = (promesa: Promise<unknown>) => promesa.catch(() => {});

  return (
    <main>
      <header className="barra-superior">
        <div>
          <div className="marca-etiqueta">{t("game.code")}</div>
          <div className="codigo">{torneo.joinCode}</div>
          <p className="tenue">{t("tournament.hint")}</p>
        </div>
        <div className="marcas">
          <div>
            <div className="marca-etiqueta">{t("tournament.round")}</div>
            <div className="marca-valor">{torneo.roundNumber}</div>
          </div>
          <div>
            <div className="marca-etiqueta">{t("tournament.teams")}</div>
            <div className="marca-valor">{torneo.standings.length}</div>
          </div>
        </div>
      </header>

      {torneo.championName !== null && (
        <section className="resumen">
          <h2>{t("tournament.champion")}</h2>
          <p className="marca-valor">{torneo.championName}</p>
        </section>
      )}

      <section>
        <div className="etiquetas">
          <button
            disabled={torneo.phase !== "LOBBY" || torneo.standings.length < 2}
            onClick={() => accion(client.startTournament(comoPartida, session.tournamentId))}
          >
            {t("tournament.start")}
          </button>
          <button
            disabled={!torneo.roundComplete || torneo.championName !== null}
            onClick={() => accion(client.nextTournamentRound(comoPartida, session.tournamentId))}
          >
            {t("tournament.next")}
          </button>
          <button
            onClick={() =>
              window.open(
                `/?torneo=${torneo.tournamentId}&token=${encodeURIComponent(session.token)}`,
                "_blank",
              )
            }
          >
            {t("projection.open")}
          </button>
        </div>
      </section>

      <div className="columnas">
        <div>
          <h2>{t("tournament.tables")}</h2>
          {torneo.tables.map((mesa, indice) => (
            <section key={indice} className="carta">
              <div className="carta-cabecera">
                <span className="carta-nombre">
                  {mesa.homeName} {mesa.awayName === null ? "" : `vs ${mesa.awayName}`}
                </span>
                <span className="etiqueta">
                  {mesa.phase === "BYE" ? t("tournament.bye") : `${t("game.round")} ${mesa.roundNumber}`}
                </span>
              </div>
              {mesa.winnerName !== null ? (
                <p>
                  {t("result.winner")} <strong>{mesa.winnerName}</strong>
                </p>
              ) : (
                <ul>
                  {Object.entries(mesa.ciaLevels).map(([pilar, nivel]) => (
                    <li key={pilar}>
                      {t(`cia.${pilar}` as TranslationKey)}: {nivel}
                    </li>
                  ))}
                </ul>
              )}
              {mesa.readyTeams.length > 0 && (
                <p className="tenue">
                  {t("ready.teams")}: {mesa.readyTeams.join(" · ")}
                </p>
              )}
            </section>
          ))}
        </div>
        <div>
          <section>
            <h2>{t("tournament.standings")}</h2>
            <ul>
              {torneo.standings.map((equipo) => (
                <li key={equipo.teamId} className={equipo.status === "ELIMINATED" ? "tenue" : ""}>
                  <strong>{equipo.displayName}</strong> — {equipo.wins} {t("tournament.wins")} ·{" "}
                  {equipo.defendedCia} {t("tournament.defended")}
                </li>
              ))}
            </ul>
          </section>
        </div>
      </div>
    </main>
  );
}
