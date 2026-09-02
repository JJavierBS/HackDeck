package com.hackdeck.adapter.websocket;

import com.hackdeck.application.limits.LimitsProperties;
import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.ParticipantSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Las conexiones de cada partida, en orden de llegada.
 *
 * Todo pasa por las funciones de calculo del mapa exterior: son atomicas por
 * clave, y asi el mapa interior no necesita ser concurrente y no hay hueco
 * entre borrar la ultima conexion de una partida y que otra se registre.
 */
@Component
public final class WebSocketSessionRegistry {

    private final Map<GameId, Map<String, ConnectedParticipant>> connectionsByGame = new ConcurrentHashMap<>();
    private final int maxPorParticipante;

    public WebSocketSessionRegistry(LimitsProperties properties) {
        this.maxPorParticipante = properties.maxSocketsPerParticipant();
    }

    /**
     * Registra la conexion y devuelve las que sobran del mismo participante,
     * para que las cierre quien llama: cerrarlas aqui dispararia la baja
     * desde dentro del cerrojo del mapa.
     *
     * Se echa a la mas antigua y no a la nueva porque la nueva es la que el
     * usuario esta mirando; rechazarla dejaria colgado a quien se reconecta
     * con una tablet que se durmio sin cerrar del todo.
     */
    public List<ConnectedParticipant> register(ParticipantSession participantSession, WebSocketSession socket) {
        List<ConnectedParticipant> sobrantes = new ArrayList<>();
        connectionsByGame.compute(participantSession.gameId(), (id, actuales) -> {
            Map<String, ConnectedParticipant> conexiones = actuales == null ? new LinkedHashMap<>() : actuales;
            conexiones.put(socket.getId(), new ConnectedParticipant(socket, participantSession));
            sobrantes.addAll(descartarSobrantes(conexiones, participantSession));
            return conexiones;
        });
        return sobrantes;
    }

    public void unregister(GameId gameId, WebSocketSession socket) {
        connectionsByGame.computeIfPresent(gameId, (id, conexiones) -> {
            conexiones.remove(socket.getId());
            return conexiones.isEmpty() ? null : conexiones;
        });
    }

    public Collection<ConnectedParticipant> connectionsFor(GameId gameId) {
        List<ConnectedParticipant> copia = new ArrayList<>();
        connectionsByGame.computeIfPresent(gameId, (id, conexiones) -> {
            copia.addAll(conexiones.values());
            return conexiones;
        });
        return List.copyOf(copia);
    }

    public Set<GameId> connectedGames() {
        return Set.copyOf(connectionsByGame.keySet());
    }

    /**
     * Sin tope, un token valido abria cuantas conexiones quisiera y el
     * difusor proyecta el estado una vez por conexion: sale mas caro para el
     * servidor que para quien las abre.
     */
    private List<ConnectedParticipant> descartarSobrantes(
            Map<String, ConnectedParticipant> conexiones, ParticipantSession participantSession) {
        List<ConnectedParticipant> delMismo = conexiones.values().stream()
                .filter(conexion -> conexion.session().participant().id()
                        .equals(participantSession.participant().id()))
                .toList();
        if (delMismo.size() <= maxPorParticipante) {
            return List.of();
        }
        List<ConnectedParticipant> sobrantes =
                List.copyOf(delMismo.subList(0, delMismo.size() - maxPorParticipante));
        sobrantes.forEach(conexion -> conexiones.remove(conexion.socket().getId()));
        return sobrantes;
    }
}
