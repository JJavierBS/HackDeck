package com.cyberrange.adapter.websocket;

import com.cyberrange.domain.model.ParticipantSession;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Gestiona el ciclo de vida de las conexiones WebSocket de una partida.
 * Cada cliente se conecta a /ws/games/{gameId}?token=...
 *
 * El canal es de solo difusion: los comandos van por REST. Los mensajes
 * entrantes se ignoran para no abrir una via alternativa sin autorizar.
 */
public final class GameWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionRegistry sessionRegistry;

    public GameWebSocketHandler(WebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionRegistry.register(participantSession(session).gameId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionRegistry.unregister(participantSession(session).gameId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    }

    private static ParticipantSession participantSession(WebSocketSession session) {
        Object attribute = session.getAttributes().get(JwtHandshakeInterceptor.PARTICIPANT_SESSION_ATTRIBUTE);
        if (attribute instanceof ParticipantSession participantSession) {
            return participantSession;
        }
        throw new IllegalStateException("Conexion WebSocket sin sesion verificada");
    }
}
