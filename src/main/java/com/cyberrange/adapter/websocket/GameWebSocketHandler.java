package com.cyberrange.adapter.websocket;

import com.cyberrange.application.port.in.GetGameStateUseCase;
import com.cyberrange.domain.model.ParticipantSession;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * El canal es de solo difusion: los comandos van por REST y los mensajes
 * entrantes se ignoran, para no abrir una via que no pase por la
 * autorizacion.
 */
public final class GameWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionRegistry sessionRegistry;
    private final GetGameStateUseCase getGameStateUseCase;
    private final GameStateWebSocketBroadcaster broadcaster;

    public GameWebSocketHandler(
            WebSocketSessionRegistry sessionRegistry,
            GetGameStateUseCase getGameStateUseCase,
            GameStateWebSocketBroadcaster broadcaster) {
        this.sessionRegistry = sessionRegistry;
        this.getGameStateUseCase = getGameStateUseCase;
        this.broadcaster = broadcaster;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession socket) {
        ParticipantSession session = participantSession(socket);
        sessionRegistry.register(session, socket);
        // Al conectar se le manda su estado, para que un cliente que se
        // reconecta no tenga que esperar a la siguiente ronda para saber
        // como esta la partida.
        broadcaster.sendStateTo(
                new ConnectedParticipant(socket, session),
                getGameStateUseCase.getGameState(session.gameId(), session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession socket, CloseStatus status) {
        sessionRegistry.unregister(participantSession(socket).gameId(), socket);
    }

    @Override
    protected void handleTextMessage(WebSocketSession socket, TextMessage message) {
    }

    private static ParticipantSession participantSession(WebSocketSession socket) {
        Object attribute = socket.getAttributes().get(JwtHandshakeInterceptor.PARTICIPANT_SESSION_ATTRIBUTE);
        if (attribute instanceof ParticipantSession participantSession) {
            return participantSession;
        }
        throw new IllegalStateException("Conexion WebSocket sin sesion verificada");
    }
}
