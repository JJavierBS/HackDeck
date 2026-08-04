package com.cyberrange.adapter.websocket;

import com.cyberrange.application.port.out.GameStateBroadcaster;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameEvent;
import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.TeamId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementacion del puerto de salida de difusion en tiempo real sobre
 * WebSocket.
 *
 * Mientras no exista el payload por rol solo se difunde lo que puede ver
 * todo el mundo: fase, ronda y equipos conectados. La triada CIA y las
 * acciones no salen por aqui; se piden por REST, que si autoriza.
 */
// TODO: Fase 5 del roadmap, difundir un payload distinto por rol.
@Component
public final class GameStateWebSocketBroadcaster implements GameStateBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(GameStateWebSocketBroadcaster.class);

    private final WebSocketSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public GameStateWebSocketBroadcaster(WebSocketSessionRegistry sessionRegistry, ObjectMapper objectMapper) {
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public void broadcastState(GameId gameId, Game game) {
        Map<String, String> teams = new LinkedHashMap<>();
        for (TeamId team : TeamId.values()) {
            game.playerOf(team).ifPresent(player -> teams.put(team.name(), player.displayName()));
        }
        send(gameId, Map.of(
                "type", "state",
                "gameId", game.id().toString(),
                "phase", game.phase().name(),
                "currentRoundNumber", game.rounds().size(),
                "teams", teams));
    }

    @Override
    public void broadcastEvents(GameId gameId, List<GameEvent> events) {
        List<Map<String, Object>> visibleEvents = events.stream()
                .filter(GameEvent::visibleToDefender)
                .map(event -> Map.<String, Object>of(
                        "roundNumber", event.roundNumber(),
                        "description", event.description(),
                        "occurredAt", event.occurredAt().toString()))
                .toList();
        if (visibleEvents.isEmpty()) {
            return;
        }
        send(gameId, Map.of("type", "events", "events", visibleEvents));
    }

    private void send(GameId gameId, Map<String, Object> payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            log.error("No se ha podido serializar el mensaje de la partida {}", gameId, e);
            return;
        }
        for (WebSocketSession session : sessionRegistry.sessionsFor(gameId)) {
            if (!session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                log.warn("No se ha podido enviar el estado a una sesion de la partida {}", gameId, e);
            }
        }
    }
}
