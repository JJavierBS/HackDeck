import { useState } from "react";
import { ApiRequestError, type GameSettingsDto, type RestClient } from "../api/restClient";
import type { GameSession, TournamentSession } from "../api/session";
import { useLanguage } from "../i18n/LanguageContext";

interface EntryViewProps {
  client: RestClient;
  onSession: (session: GameSession) => void;
  onTournament: (session: TournamentSession) => void;
}

const DEFAULT_SETTINGS: Required<GameSettingsDto> = {
  roundsPerHalf: 6,
  roundTimeoutSeconds: 90,
  initialBudget: 20,
  incomePerRound: 10,
};

export function EntryView({ client, onSession, onTournament }: EntryViewProps) {
  const { t } = useLanguage();
  const [code, setCode] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [instructorKey, setInstructorKey] = useState("");
  const [settings, setSettings] = useState(DEFAULT_SETTINGS);
  const [error, setError] = useState<string | null>(null);

  const fallo = (cause: unknown) =>
    setError(cause instanceof ApiRequestError ? cause.message : t("entry.error.network"));

  /** El equipo no sabe si el codigo es de una partida o de un torneo. */
  const entrar = () => {
    setError(null);
    client
      .joinGame(code, displayName)
      .then((respuesta) => {
        if (respuesta.kind === "TOURNAMENT") {
          onTournament({
            tournamentId: respuesta.tournamentId!,
            joinCode: respuesta.joinCode,
            token: respuesta.token,
            instructor: false,
          });
          return;
        }
        onSession({
          gameId: respuesta.gameId!,
          joinCode: respuesta.joinCode,
          team: respuesta.team,
          token: respuesta.token,
        });
      })
      .catch(fallo);
  };

  const run = (action: Promise<GameSession>) => {
    setError(null);
    action.then(onSession).catch(fallo);
  };

  const numberField = (field: keyof GameSettingsDto) => ({
    type: "number",
    value: settings[field],
    onChange: (event: React.ChangeEvent<HTMLInputElement>) =>
      setSettings({ ...settings, [field]: Number(event.target.value) }),
  });

  return (
    <main>
      <h1>{t("app.title")}</h1>

      <div className="columnas">
        <section>
          <h2>{t("entry.team.title")}</h2>
          <label>
            {t("entry.team.code")}
            <input
              value={code}
              onChange={(event) => setCode(event.target.value.toUpperCase())}
              maxLength={6}
              autoCapitalize="characters"
            />
          </label>
          <label>
            {t("entry.team.name")}
            <input
              value={displayName}
              onChange={(event) => setDisplayName(event.target.value)}
              maxLength={24}
            />
          </label>
          <button
            disabled={code.length < 6 || displayName.trim().length === 0}
            onClick={entrar}
          >
            {t("entry.team.join")}
          </button>
        </section>

        <section>
          <h2>{t("entry.instructor.title")}</h2>
          <label>
            {t("entry.instructor.rounds")}
            <input {...numberField("roundsPerHalf")} />
          </label>
          <label>
            {t("entry.instructor.timeout")}
            <input {...numberField("roundTimeoutSeconds")} />
          </label>
          <label>
            {t("entry.instructor.budget")}
            <input {...numberField("initialBudget")} />
          </label>
          <label>
            {t("entry.instructor.income")}
            <input {...numberField("incomePerRound")} />
          </label>
          <label>
            {t("entry.instructor.key")}
            <input value={instructorKey} onChange={(event) => setInstructorKey(event.target.value)} />
          </label>
          <div className="etiquetas">
            <button onClick={() => run(client.createGame(settings, instructorKey || undefined))}>
              {t("entry.instructor.create")}
            </button>
            <button
              onClick={() => {
                setError(null);
                client
                  .createTournament(settings, instructorKey || undefined)
                  .then((respuesta) =>
                    onTournament({
                      tournamentId: respuesta.tournamentId,
                      joinCode: respuesta.joinCode,
                      token: respuesta.token,
                      instructor: true,
                    }),
                  )
                  .catch(fallo);
              }}
            >
              {t("tournament.create")}
            </button>
          </div>
        </section>
      </div>

      {error !== null && <p className="error">{error}</p>}
    </main>
  );
}
