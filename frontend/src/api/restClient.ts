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
  cardName: Record<string, string> | null;
  side: "ATTACKER" | "DEFENDER" | null;
  /** null significa que aguanta hasta el final de la mitad. */
  roundsRemaining: number | null;
}

export interface QueuedActionDto {
  intentId: string;
  cardId: string;
  parameters: Record<string, string>;
}

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
  duration: "INSTANT" | "ROUNDS" | "PERMANENT";
  impact: Record<string, number>;
  counters: Record<string, number>;
  effects: string[];
}

export interface EventDetailDto {
  success: boolean | null;
  failureReason: "COUNTERED" | "BAD_LUCK" | null;
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
  cardName: Record<string, string> | null;
  description: string;
  detail: EventDetailDto | null;
  occurredAt: string;
}

/**
 * Lo que el servidor deja ver a este rol. Los campos que no le
 * corresponden llegan a null: no es que se oculten al pintar, es que no
 * vienen en la respuesta.
 */
export interface HalfSummaryDto {
  number: number;
  attackingTeam: "A" | "B";
  defendingTeam: "A" | "B";
  ciaLevels: Record<string, number>;
  defendedCia: number;
  takedownRound: number | null;
}

export interface GameStateDto {
  gameId: string;
  joinCode: string;
  phase: "PREPARATION" | "IN_PROGRESS" | "FINISHED";
  ciaLevels: Record<string, number> | null;
  /** Como quedo la mitad ya cerrada: la marca que hay que batir. */
  previousHalf: HalfSummaryDto | null;
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
  /** Defensas del rival, solo si se ha pagado por averiguarlas. */
  revealedRivalCards: ActiveCardDto[];
  yourQueuedActions: QueuedActionDto[];
  events: GameEventDto[];
  result: MatchResultDto | null;
}

export interface TournamentTableDto {
  gameId: string | null;
  instructorToken: string | null;
  homeName: string;
  awayName: string | null;
  phase: string;
  halfNumber: number | null;
  roundNumber: number;
  ciaLevels: Record<string, number>;
  readyTeams: string[];
  winnerName: string | null;
}

export interface TournamentDto {
  tournamentId: string;
  joinCode: string;
  phase: "LOBBY" | "IN_PROGRESS" | "FINISHED";
  roundNumber: number;
  roundComplete: boolean;
  championName: string | null;
  standings: { teamId: string; displayName: string; status: string; wins: number; defendedCia: number }[];
  tables: TournamentTableDto[];
}

export interface TournamentAccessDto {
  tournamentId: string;
  joinCode: string;
  token: string;
}

/** Donde juega ahora el equipo; el cliente lo consulta y entra sin mas. */
export interface PlacementDto {
  status: "WAITING" | "PLAYING" | "ELIMINATED" | "CHAMPION";
  gameId: string | null;
  gameToken: string | null;
  team: "A" | "B" | null;
  roundNumber: number;
}

export interface ConnectionInfoDto {
  urls: string[];
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
  joinGame(code: string, displayName: string): Promise<JoinDto>;
  startGame(session: GameSession): Promise<void>;
  getGameState(session: GameSession): Promise<GameStateDto>;
  enqueueAction(
    session: GameSession,
    action: { cardId: string; parameters: Record<string, string> },
  ): Promise<void>;
  dequeueAction(session: GameSession, intentId: string): Promise<void>;
  reorderQueue(session: GameSession, intentIds: string[]): Promise<void>;
  resolveRound(session: GameSession): Promise<GameStateDto>;
  getCatalog(session: GameSession): Promise<CardDto[]>;
  launchTwist(session: GameSession, cardId: string): Promise<void>;
  getHistory(session: GameSession): Promise<MatchHistoryDto>;
  markReady(session: GameSession): Promise<void>;
  setAutoResolve(session: GameSession, enabled: boolean): Promise<void>;
  closeHalf(session: GameSession): Promise<void>;
  closeMatch(session: GameSession): Promise<void>;
  getConnectionInfo(): Promise<ConnectionInfoDto>;
  createTournament(settings: GameSettingsDto, instructorKey?: string): Promise<TournamentAccessDto>;
  getTournament(session: GameSession, tournamentId: string): Promise<TournamentDto>;
  startTournament(session: GameSession, tournamentId: string): Promise<void>;
  nextTournamentRound(session: GameSession, tournamentId: string): Promise<void>;
  getPlacement(tournamentToken: string): Promise<PlacementDto>;
}

/** Respuesta de unirse: puede ser una partida suelta o un torneo. */
export interface JoinDto {
  kind: "GAME" | "TOURNAMENT";
  gameId: string | null;
  tournamentId: string | null;
  joinCode: string;
  team: "A" | "B" | null;
  token: string;
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
      request<JoinDto>("POST", "/api/v1/games/join", { body: { code, displayName } }),

    startGame: (session) =>
      request<void>("POST", `/api/v1/games/${session.gameId}/start`, { session }),

    getGameState: (session) =>
      request<GameStateDto>("GET", `/api/v1/games/${session.gameId}`, { session }),

    enqueueAction: (session, action) =>
      request<void>("POST", `/api/v1/games/${session.gameId}/actions`, { session, body: action }),

    dequeueAction: (session, intentId) =>
      request<void>("DELETE", `/api/v1/games/${session.gameId}/actions/${intentId}`, { session }),

    reorderQueue: (session, intentIds) =>
      request<void>("PUT", `/api/v1/games/${session.gameId}/actions/reorder`, { session, body: { intentIds } }),

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

    getConnectionInfo: () => request<ConnectionInfoDto>("GET", "/api/v1/connection"),

    createTournament: (settings, instructorKey) =>
      request<TournamentAccessDto>("POST", "/api/v1/tournaments", {
        body: settings,
        headers: instructorKey ? { "X-Instructor-Key": instructorKey } : undefined,
      }),

    getTournament: (session, tournamentId) =>
      request<TournamentDto>("GET", `/api/v1/tournaments/${tournamentId}`, { session }),

    startTournament: (session, tournamentId) =>
      request<void>("POST", `/api/v1/tournaments/${tournamentId}/start`, { session }),

    nextTournamentRound: (session, tournamentId) =>
      request<void>("POST", `/api/v1/tournaments/${tournamentId}/rounds/next`, { session }),

    getPlacement: (tournamentToken) =>
      request<PlacementDto>("GET", "/api/v1/tournaments/me", {
        session: { gameId: "", joinCode: "", team: null, token: tournamentToken },
      }),
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
