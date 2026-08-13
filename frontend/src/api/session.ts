export type ParticipantTeam = "A" | "B" | null;

export interface GameSession {
  gameId: string;
  joinCode: string;
  team: ParticipantTeam;
  token: string;
}

/** Identidad de torneo: dura todo el torneo aunque cambie de mesa. */
export interface TournamentSession {
  tournamentId: string;
  joinCode: string;
  token: string;
  /** El instructor gobierna el torneo; los equipos solo juegan. */
  instructor: boolean;
}

const TOURNAMENT_KEY = "hackdeck.tournament";

export function loadTournament(): TournamentSession | null {
  const raw = sessionStorage.getItem(TOURNAMENT_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as TournamentSession;
  } catch {
    sessionStorage.removeItem(TOURNAMENT_KEY);
    return null;
  }
}

export function saveTournament(session: TournamentSession): void {
  sessionStorage.setItem(TOURNAMENT_KEY, JSON.stringify(session));
}

export function clearTournament(): void {
  sessionStorage.removeItem(TOURNAMENT_KEY);
}

const STORAGE_KEY = "hackdeck.session";

/**
 * La sesion sobrevive a un F5 para que recargar no eche al equipo de la
 * partida. Se guarda en sessionStorage y no en localStorage para que dos
 * pestanas del mismo navegador puedan ser equipos distintos al probar.
 */
export function loadSession(): GameSession | null {
  const raw = sessionStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as GameSession;
  } catch {
    sessionStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function saveSession(session: GameSession): void {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

export function clearSession(): void {
  sessionStorage.removeItem(STORAGE_KEY);
}
