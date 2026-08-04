export type ParticipantTeam = "A" | "B" | null;

export interface GameSession {
  gameId: string;
  joinCode: string;
  team: ParticipantTeam;
  token: string;
}

const STORAGE_KEY = "cyberrange.session";

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
