package com.cyberrange.adapter.websocket;

import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class WebSocketSessionRegistry {

    private final Map<GameId, Map<String, ConnectedParticipant>> connectionsByGame = new ConcurrentHashMap<>();

    public void register(ParticipantSession participantSession, WebSocketSession socket) {
        connectionsByGame
                .computeIfAbsent(participantSession.gameId(), id -> new ConcurrentHashMap<>())
                .put(socket.getId(), new ConnectedParticipant(socket, participantSession));
    }

    public void unregister(GameId gameId, WebSocketSession socket) {
        Map<String, ConnectedParticipant> connections = connectionsByGame.get(gameId);
        if (connections == null) {
            return;
        }
        connections.remove(socket.getId());
        if (connections.isEmpty()) {
            connectionsByGame.remove(gameId);
        }
    }

    public Collection<ConnectedParticipant> connectionsFor(GameId gameId) {
        return List.copyOf(connectionsByGame.getOrDefault(gameId, Map.of()).values());
    }

    public Set<GameId> connectedGames() {
        return Set.copyOf(connectionsByGame.keySet());
    }
}
