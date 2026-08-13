package com.cyberrange.adapter.websocket;

import com.cyberrange.application.port.out.GameStateBroadcaster;
import com.cyberrange.application.service.GameViewProjector;
import com.cyberrange.application.view.GameView;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public final class GameStateWebSocketBroadcaster implements GameStateBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(GameStateWebSocketBroadcaster.class);

    private final WebSocketSessionRegistry sessionRegistry;
    private final GameViewProjector projector;
    private final ObjectMapper objectMapper;

    public GameStateWebSocketBroadcaster(
            WebSocketSessionRegistry sessionRegistry,
            GameViewProjector projector,
            ObjectMapper objectMapper) {
        this.sessionRegistry = sessionRegistry;
        this.projector = projector;
        this.objectMapper = objectMapper;
    }

    @Override
    public void broadcastState(GameId gameId, Game game) {
        for (ConnectedParticipant connection : sessionRegistry.connectionsFor(gameId)) {
            GameView view = projector.project(game, connection.session().participant());
            send(connection.socket(), gameId, view);
        }
    }

    public void sendStateTo(ConnectedParticipant connection, Game game) {
        send(connection.socket(), game.id(), projector.project(game, connection.session().participant()));
    }

    private void send(WebSocketSession socket, GameId gameId, GameView view) {
        if (!socket.isOpen()) {
            return;
        }
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "state");
        message.put("state", view);
        try {
            socket.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (JacksonException e) {
            log.error("No se ha podido serializar el estado de la partida {}", gameId, e);
        } catch (IOException e) {
            log.warn("No se ha podido enviar el estado a una sesion de la partida {}", gameId, e);
        }
    }
}
