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

export interface MatchResultDto {
  winner: "A" | "B" | null;
  outcome: "TAKEDOWN" | "TAKEDOWN_FASTER" | "POINTS" | "DRAW";
  defendedCia: Record<string, number>;
  takedownRound: Record<string, number>;
}

export interface HistoryEventDto {
  halfNumber: number;
  roundNumber: number;
  type:
    | "TEAM_JOINED"
    | "MATCH_STARTED"
    | "HALF_STARTED"
    | "ATTACK"
    | "DEFENCE"
    | "TWIST_LAUNCHED"
    | "ROUND_RESOLVED"
    | "MATCH_FINISHED";
  actor: "ATTACKER" | "DEFENDER" | null;
  cardId: string | null;
  description: string;
  /** Solo viene en el cierre de ronda. */
  ciaAfter: Record<string, number>;
  occurredAt: string;
}

export interface MatchHistoryDto {
  gameId: string;
  joinCode: string;
  phase: string;
  settings: {
    roundsPerHalf: number;
    roundTimeoutSeconds: number;
    initialBudget: number;
    incomePerRound: number;
  };
  teams: Record<string, string>;
  events: HistoryEventDto[];
  result: MatchResultDto | null;
}

export interface ActiveCardDto {
  cardId: string;
  side: "ATTACKER" | "DEFENDER" | null;
  /** null significa que aguanta hasta el final de la mitad. */
  roundsRemaining: number | null;
}

export interface QueuedActionDto {
  cardId: string;
  parameters: Record<string, string>;
}

/** Carta del catalogo. El coste, el ruido y el efecto los fija el servidor. */
export interface CardDto {
  id: string;
  type: "ACTION" | "POWERUP" | "TWIST";
  side: "ATTACKER" | "DEFENDER" | null;
  phase: "RECON" | "ACCESS" | "ESCALATION" | "IMPACT" | null;
  category: "HYGIENE" | "ARCHITECTURE" | "DETECTION" | "RESPONSE" | null;
  name: Record<string, string>;
  description: Record<string, string>;
  cost: number;
  noise: "NONE" | "LOW" | "MEDIUM" | "HIGH";
  successRate: number;
  impact: Record<string, number>;
  counters: Record<string, number>;
}

/** Lo que le paso a una accion, para contarselo a quien la jugo. */
export interface EventDetailDto {
  success: boolean | null;
  failureReason: "KILL_CHAIN" | "COUNTERED" | "BAD_LUCK" | null;
  impact: Record<string, number>;
  mitigated: number;
  unlocked: string[];
  boosts: string[];
  detected: boolean | null;
  /** Llega vacio al atacante: las defensas del rival se averiguan con recon. */
  counteredBy: string | null;
}

export interface GameEventDto {
  halfNumber: number;
  roundNumber: number;
  type: string;
  actor: "ATTACKER" | "DEFENDER" | null;
  cardId: string | null;
  description: string;
  detail: EventDetailDto | null;
  occurredAt: string;
}

/**
 * Lo que el servidor deja ver a este rol. Los campos que no le
 * corresponden llegan a null: no es que se oculten al pintar, es que no
 * vienen en la respuesta.
 */
export interface GameStateDto {
  gameId: string;
  joinCode: string;
  phase: "PREPARATION" | "IN_PROGRESS" | "FINISHED";
  ciaLevels: Record<string, number> | null;
  halfNumber: number | null;
  currentRoundNumber: number;
  roundsPerHalf: number;
  roundTimeoutSeconds: number;
  teams: Record<string, string>;
  yourTeam: "A" | "B" | null;
  yourSide: "ATTACKER" | "DEFENDER" | null;
  yourBudget: number | null;
  budgets: Record<string, number> | null;
  attackingTeam: "A" | "B" | null;
  roundDeadlineAt: string | null;
  autoResolve: boolean;
  readyTeams: string[];
  /** Solo llega al instructor: su panel ya no es lo que se proyecta. */
  queuedBySide: Record<string, QueuedActionDto[]> | null;
  yourKillChain: string[];
  yourActiveCards: ActiveCardDto[];
  yourQueuedActions: QueuedActionDto[];
  events: GameEventDto[];
  result: MatchResultDto | null;
}

export interface GameSettingsDto {
  roundsPerHalf?: number;
  roundTimeoutSeconds?: number;
  initialBudget?: number;
  incomePerRound?: number;
}

export interface EnqueueActionDto {
  cardId: string;
  parameters: Record<string, string>;
}

export interface RestClient {
  createGame(settings: GameSettingsDto, instructorKey?: string): Promise<GameSession>;
  joinGame(code: string, displayName: string): Promise<GameSession>;
  startGame(session: GameSession): Promise<void>;
  getGameState(session: GameSession): Promise<GameStateDto>;
  enqueueAction(session: GameSession, action: EnqueueActionDto): Promise<void>;
  resolveRound(session: GameSession): Promise<GameStateDto>;
  getCatalog(session: GameSession): Promise<CardDto[]>;
  launchTwist(session: GameSession, cardId: string): Promise<void>;
  getHistory(session: GameSession): Promise<MatchHistoryDto>;
  markReady(session: GameSession): Promise<void>;
  setAutoResolve(session: GameSession, enabled: boolean): Promise<void>;
  closeHalf(session: GameSession): Promise<void>;
  closeMatch(session: GameSession): Promise<void>;
}

/**
 * En desarrollo el front corre en Vite y el backend en otro puerto; en
 * produccion el jar sirve ambos y basta con rutas relativas.
 */
const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "";

export function createRestClient(): RestClient {
  return {
    createGame: (settings, instructorKey) =>
      request<GameSession>("POST", "/api/v1/games", {
        body: settings,
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
      request<GameStateDto>("POST", `/api/v1/games/${session.gameId}/rounds/resolve`, { session }),

    getCatalog: (session) => request<CardDto[]>("GET", "/api/v1/catalog", { session }),

    launchTwist: (session, cardId) =>
      request<void>("POST", `/api/v1/games/${session.gameId}/twists/${cardId}`, { session }),

    getHistory: (session) =>
      request<MatchHistoryDto>("GET", `/api/v1/games/${session.gameId}/history`, { session }),

    markReady: (session) =>
      request<void>("POST", `/api/v1/games/${session.gameId}/rounds/ready`, { session }),

    setAutoResolve: (session, enabled) =>
      request<void>("POST", `/api/v1/games/${session.gameId}/auto-resolve/${enabled}`, { session }),

    closeHalf: (session) => request<void>("POST", `/api/v1/games/${session.gameId}/half/close`, { session }),

    closeMatch: (session) => request<void>("POST", `/api/v1/games/${session.gameId}/close`, { session }),
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
