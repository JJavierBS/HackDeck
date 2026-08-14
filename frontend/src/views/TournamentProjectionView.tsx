import { useEffect, useState } from "react";
import type { RestClient, TournamentDto } from "../api/restClient";
import type { GameSession, TournamentSession } from "../api/session";
import { Loading } from "../components/Loading";
import { useLanguage } from "../i18n/LanguageContext";

/**
 * La rejilla del canon: todas las mesas a la vez. Sin registro ni cartas,
 * que en la pared se leerian los ataques que nadie ha detectado.
 */
export function TournamentProjectionView({
  client,
  session,
}: {
  client: RestClient;
  session: TournamentSession;
}) {
  const { t } = useLanguage();
  const [torneo, setTorneo] = useState<TournamentDto | null>(null);
  const comoPartida: GameSession = {
    gameId: session.tournamentId,
    joinCode: "",
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
      <main className="proyeccion">
        <Loading big />
      </main>
    );
  }

  return (
    <main className="proyeccion">
      <header className="proyeccion-cabecera">
        <div>
          <div className="marca-etiqueta">{t("projection.join")}</div>
          <div className="codigo codigo-gigante">{torneo.joinCode}</div>
        </div>
        <div className="marcas">
          <div>
            <div className="marca-etiqueta">{t("tournament.round")}</div>
            <div className="codigo">{torneo.roundNumber}</div>
          </div>
        </div>
      </header>

      {torneo.championName !== null && (
        <section className="proyeccion-aviso">
          {t("tournament.champion")}: {torneo.championName}
        </section>
      )}

      <div className="rejilla-mesas">
        {torneo.tables.map((mesa, indice) => (
          <section key={indice}>
            <div className="marca-valor">
              {mesa.homeName} {mesa.awayName === null ? "" : `vs ${mesa.awayName}`}
            </div>
            {mesa.winnerName !== null ? (
              <p className="marca-etiqueta">
                {t("result.winner")} {mesa.winnerName}
              </p>
            ) : (
              <>
                <div className="marca-etiqueta">
                  {t("game.round")} {mesa.roundNumber}
                </div>
                {Object.entries(mesa.ciaLevels).map(([pilar, nivel]) => (
                  <div className="pilar" key={pilar}>
                    <div className="pilar-barra">
                      <div
                        className={`pilar-relleno ${nivel < 40 ? "critico" : nivel < 80 ? "tocado" : ""}`}
                        style={{ width: `${nivel}%` }}
                      />
                    </div>
                  </div>
                ))}
              </>
            )}
          </section>
        ))}
      </div>
    </main>
  );
}
