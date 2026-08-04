import type { GameSession } from "./session";

export interface RealtimeMessage {
  type: string;
  [key: string]: unknown;
}

/**
 * Cliente de tiempo real hacia el backend (WebSocket)
 */
export interface RealtimeClient {
  connect(session: GameSession): void;
  disconnect(): void;
  onMessage(handler: (message: RealtimeMessage) => void): void;
}

const INITIAL_RETRY_MS = 500;
const MAX_RETRY_MS = 10_000;

export function createRealtimeClient(): RealtimeClient {
  let socket: WebSocket | null = null;
  let handler: (message: RealtimeMessage) => void = () => {};
  let retryMs = INITIAL_RETRY_MS;
  let retryTimer: ReturnType<typeof setTimeout> | null = null;
  let closedByUs = false;

  function open(session: GameSession): void {
    socket = new WebSocket(realtimeUrl(session));

    socket.onopen = () => {
      retryMs = INITIAL_RETRY_MS;
    };

    socket.onmessage = (event) => {
      try {
        handler(JSON.parse(event.data as string) as RealtimeMessage);
      } catch {
        // Un mensaje ilegible no debe tumbar la conexion de la partida.
      }
    };

    socket.onclose = () => {
      if (closedByUs) {
        return;
      }
      retryTimer = setTimeout(() => open(session), retryMs);
      retryMs = Math.min(retryMs * 2, MAX_RETRY_MS);
    };
  }

  return {
    connect(session) {
      closedByUs = false;
      open(session);
    },

    disconnect() {
      closedByUs = true;
      if (retryTimer !== null) {
        clearTimeout(retryTimer);
        retryTimer = null;
      }
      socket?.close();
      socket = null;
    },

    onMessage(newHandler) {
      handler = newHandler;
    },
  };
}

/**
 * El protocolo se deduce del de la pagina para que funcione igual servido
 * por http en el aula que tras un proxy https. El token va en la query
 * porque el WebSocket del navegador no admite cabeceras.
 */
function realtimeUrl(session: GameSession): string {
  const base = import.meta.env.VITE_API_BASE_URL ?? window.location.origin;
  const url = new URL(base);
  url.protocol = url.protocol === "https:" ? "wss:" : "ws:";
  url.pathname = `/ws/games/${session.gameId}`;
  url.searchParams.set("token", session.token);
  return url.toString();
}
