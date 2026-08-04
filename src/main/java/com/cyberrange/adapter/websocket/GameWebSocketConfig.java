package com.cyberrange.adapter.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Registra el endpoint WebSocket /ws/games/{gameId}.
 *
 * Se aceptan todos los origenes a proposito: quien decide si la conexion
 * entra es el token, no la cabecera Origin, y en el aula los equipos se
 * conectan por la IP local del portatil del instructor, que no se conoce
 * de antemano.
 */
@Configuration
@EnableWebSocket
public class GameWebSocketConfig implements WebSocketConfigurer {

    private final WebSocketSessionRegistry sessionRegistry;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    public GameWebSocketConfig(
            WebSocketSessionRegistry sessionRegistry,
            JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.sessionRegistry = sessionRegistry;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new GameWebSocketHandler(sessionRegistry), "/ws/games/{gameId}")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
