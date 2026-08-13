package com.hackdeck.adapter.websocket;

import com.hackdeck.domain.model.GameId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.PingMessage;

import java.io.IOException;

/**
 * Mantiene vivas las conexiones y limpia las que ya no lo estan. En el aula
 * las tablets se duermen y los proxies cierran conexiones ociosas: sin un
 * ping periodico el servidor seguiria difundiendo a sesiones fantasma.
 */
@Component
public final class WebSocketHeartbeat {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHeartbeat.class);

    private final WebSocketSessionRegistry sessionRegistry;

    public WebSocketHeartbeat(WebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Scheduled(fixedDelayString = "${hackdeck.websocket.heartbeat-millis:20000}")
    public void pingConnections() {
        for (GameId gameId : sessionRegistry.connectedGames()) {
            for (ConnectedParticipant connection : sessionRegistry.connectionsFor(gameId)) {
                if (!connection.socket().isOpen()) {
                    sessionRegistry.unregister(gameId, connection.socket());
                    continue;
                }
                try {
                    connection.socket().sendMessage(new PingMessage());
                } catch (IOException | IllegalStateException e) {
                    log.debug("Sesion caida en la partida {}, se descarta", gameId);
                    sessionRegistry.unregister(gameId, connection.socket());
                }
            }
        }
    }
}
