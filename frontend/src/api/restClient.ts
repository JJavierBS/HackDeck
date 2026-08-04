import type { GameSession } from "./session";

export interface ApiError {
  error: string;
  message: string;
}

export class ApiRequestError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, body: ApiError) {
    super(body.message);
    this.status = status;
    this.code = body.error;
  }
}

export interface GameStateDto {
  gameId: string;
  joinCode: string;
  phase: "PREPARATION" | "IN_PROGRESS" | "FINISHED";
  ciaLevels: Record<string, number>;
  currentRoundNumber: number;
  teams: Record<string, string>;
  yourTeam: "A" | "B" | null;
  yourSide: "ATTACKER" | "DEFENDER" | null;
}

export interface EnqueueActionDto {
  actionType: string;
  parameters: Record<string, string>;
  noisy: boolean;
}

export interface RestClient {
  createGame(instructorKey?: string): Promise<GameSession>;
  joinGame(code: string, displayName: string): Promise<GameSession>;
  startGame(session: GameSession): Promise<void>;
  getGameState(session: GameSession): Promise<GameStateDto>;
  enqueueAction(session: GameSession, action: EnqueueActionDto): Promise<void>;
  resolveRound(session: GameSession): Promise<void>;
}

/**
 * En desarrollo el front corre en Vite y el backend en otro puerto; en
 * produccion el jar sirve ambos y basta con rutas relativas.
 */
const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "";

export function createRestClient(): RestClient {
  return {
    createGame: (instructorKey) =>
      request<GameSession>("POST", "/api/v1/games", {
        headers: instructorKey ? { "X-Instructor-Key": instructorKey } : undefined,
      }),

    joinGame: (code, displayName) =>
      request<GameSession>("POST", "/api/v1/games/join", { body: { code, displayName } }),

    startGame: (session) =>
      request<void>("POST", `/api/v1/games/${session.gameId}/start`, { session }),

    getGameState: (session) =>
      request<GameStateDto>("GET", `/api/v1/games/${session.gameId}`, { session }),

    enqueueAction: (session, action) =>
      request<void>("POST", `/api/v1/games/${session.gameId}/actions`, { session, body: action }),

    resolveRound: (session) =>
      request<void>("POST", `/api/v1/games/${session.gameId}/rounds/resolve`, { session }),
  };
}

interface RequestOptions {
  session?: GameSession;
  body?: unknown;
  headers?: Record<string, string>;
}

async function request<T>(method: string, path: string, options: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = { ...options.headers };
  if (options.session) {
    headers.Authorization = `Bearer ${options.session.token}`;
  }
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

  if (!response.ok) {
    throw new ApiRequestError(response.status, await readError(response));
  }
  return (await readBody(response)) as T;
}

async function readError(response: Response): Promise<ApiError> {
  try {
    return (await response.json()) as ApiError;
  } catch {
    return { error: "error_desconocido", message: response.statusText };
  }
}

async function readBody(response: Response): Promise<unknown> {
  const text = await response.text();
  return text.length === 0 ? undefined : JSON.parse(text);
}
