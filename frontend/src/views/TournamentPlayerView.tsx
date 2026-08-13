import { useEffect, useState } from "react";
import type { PlacementDto, RestClient } from "../api/restClient";
import type { GameSession, TournamentSession } from "../api/session";
import { useLanguage } from "../i18n/LanguageContext";

interface Props {
  client: RestClient;
  session: TournamentSession;
  onTable: (mesa: GameSession) => void;
}

/**
 * Lo que ve el equipo entre partidas. Pregunta cada poco donde le toca
 * jugar y entra solo: cambiar de mesa no le cuesta ni un clic.
 */
export function TournamentPlayerView({ client, session, onTable }: Props) {
  const { t } = useLanguage();
  const [sitio, setSitio] = useState<PlacementDto | null>(null);

  useEffect(() => {
    let activo = true;
    const preguntar = () =>
      client
        .getPlacement(session.token)
        .then((respuesta) => {
          if (!activo) {
            return;
          }
          setSitio(respuesta);
          if (respuesta.status === "PLAYING" && respuesta.gameId && respuesta.gameToken) {
            onTable({
              gameId: respuesta.gameId,
              joinCode: session.joinCode,
              team: respuesta.team,
              token: respuesta.gameToken,
            });
          }
        })
        .catch(() => {});
    preguntar();
    const reloj = setInterval(preguntar, 2000);
    return () => {
      activo = false;
      clearInterval(reloj);
    };
  }, [client, session, onTable]);

  const mensaje = () => {
    if (sitio === null) {
      return t("app.loading");
    }
    if (sitio.status === "ELIMINATED") {
      return t("tournament.eliminated");
    }
    if (sitio.status === "CHAMPION") {
      return t("tournament.champion");
    }
    return t("tournament.waiting");
  };

  return (
    <main>
      <h1>{t("tournament.title")}</h1>
      <section>
        <p className="marca-valor">{mensaje()}</p>
        {sitio !== null && sitio.roundNumber > 0 && (
          <p className="tenue">
            {t("tournament.round")} {sitio.roundNumber}
          </p>
        )}
      </section>
    </main>
  );
}
