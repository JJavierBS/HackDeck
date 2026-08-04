package com.cyberrange.adapter.websocket;

import com.cyberrange.application.port.out.AccessTokenPort;
import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.ParticipantSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

/**
 * Autentica la conexion en el propio handshake: si el token falta, no vale
 * o es de otra partida, la conexion se rechaza y nunca llega a registrarse.
 *
 * El token viaja como parametro de consulta porque el WebSocket del
 * navegador no permite enviar cabeceras. Por eso conviene no registrar las
 * URL completas en los logs del proxy.
 */
@Component
public final class JwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String PARTICIPANT_SESSION_ATTRIBUTE = "participantSession";

    private static final String TOKEN_PARAMETER = "token";

    private final AccessTokenPort accessTokenPort;

    public JwtHandshakeInterceptor(AccessTokenPort accessTokenPort) {
        this.accessTokenPort = accessTokenPort;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        URI uri = request.getURI();
        String token = UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst(TOKEN_PARAMETER);
        Optional<GameId> gameId = gameIdFrom(uri);
        Optional<ParticipantSession> session = accessTokenPort.verify(token);
        if (gameId.isEmpty() || session.isEmpty() || !session.get().belongsTo(gameId.get())) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        attributes.put(PARTICIPANT_SESSION_ATTRIBUTE, session.get());
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
    }

    private static Optional<GameId> gameIdFrom(URI uri) {
        String path = uri.getPath();
        try {
            return Optional.of(GameId.of(path.substring(path.lastIndexOf('/') + 1)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
