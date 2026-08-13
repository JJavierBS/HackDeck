import { useState } from "react";
import type { GameSettingsDto, RestClient } from "../api/restClient";
import type { GameSession, TournamentSession } from "../api/session";
import { ErrorAlert } from "../components/ErrorAlert";
import { NumericStepper } from "../components/NumericStepper";
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
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState<"joining" | "creating" | null>(null);

  const fallo = (cause: unknown) => {
    setLoading(null);
    setError(cause);
  };

  /** El equipo no sabe si el codigo es de una partida o de un torneo. */
  const entrar = () => {
    setError(null);
    setLoading("joining");
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

  const crearPartida = () => {
    setError(null);
    setLoading("creating");
    client
      .createGame(settings, instructorKey || undefined)
      .then(onSession)
      .catch(fallo);
  };

  const crearTorneo = () => {
    setError(null);
    setLoading("creating");
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
  };

  const updateSetting = (field: keyof GameSettingsDto, val: number) => {
    setSettings({ ...settings, [field]: val });
  };

  return (
    <main>
      <h1>
        Hack<span className="acento-texto">Deck</span>
      </h1>

      {loading !== null && (
        <div className="indicador-carga">
          <span className="spinner-mono" />
          {t(loading === "creating" ? "entry.creating" : "entry.joining")}
        </div>
      )}

      <ErrorAlert error={error} />

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
              disabled={loading !== null}
            />
          </label>
          <label>
            {t("entry.team.name")}
            <input
              value={displayName}
              onChange={(event) => setDisplayName(event.target.value)}
              maxLength={24}
              disabled={loading !== null}
            />
          </label>
          <button
            disabled={loading !== null || code.length < 6 || displayName.trim().length === 0}
            onClick={entrar}
          >
            {t("entry.team.join")}
          </button>
        </section>

        <section>
          <h2>{t("entry.instructor.title")}</h2>
          <div className="grid-configuracion">
            <label>
              {t("entry.instructor.rounds")}
              <NumericStepper
                value={settings.roundsPerHalf}
                onChange={(val) => updateSetting("roundsPerHalf", val)}
                min={1}
                max={20}
                disabled={loading !== null}
              />
            </label>
            <label>
              {t("entry.instructor.timeout")}
              <NumericStepper
                value={settings.roundTimeoutSeconds}
                onChange={(val) => updateSetting("roundTimeoutSeconds", val)}
                min={10}
                max={600}
                step={5}
                disabled={loading !== null}
              />
            </label>
            <label>
              {t("entry.instructor.budget")}
              <NumericStepper
                value={settings.initialBudget}
                onChange={(val) => updateSetting("initialBudget", val)}
                min={0}
                max={100}
                disabled={loading !== null}
              />
            </label>
            <label>
              {t("entry.instructor.income")}
              <NumericStepper
                value={settings.incomePerRound}
                onChange={(val) => updateSetting("incomePerRound", val)}
                min={0}
                max={50}
                disabled={loading !== null}
              />
            </label>
          </div>
          <label>
            {t("entry.instructor.key")}
            <input
              type="password"
              value={instructorKey}
              onChange={(event) => setInstructorKey(event.target.value)}
              disabled={loading !== null}
            />
          </label>
          <div className="grupo-botones">
            <button disabled={loading !== null} onClick={crearPartida}>
              {t("entry.instructor.create")}
            </button>
            <button disabled={loading !== null} onClick={crearTorneo}>
              {t("tournament.create")}
            </button>
          </div>
        </section>
      </div>
    </main>
  );
}
