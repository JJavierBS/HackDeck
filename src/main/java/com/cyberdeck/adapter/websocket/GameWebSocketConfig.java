package com.cyberdeck.adapter.websocket;

import com.cyberdeck.application.port.in.GetGameStateUseCase;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
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
@EnableScheduling
public class GameWebSocketConfig implements WebSocketConfigurer {

    private final WebSocketSessionRegistry sessionRegistry;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final GetGameStateUseCase getGameStateUseCase;
    private final GameStateWebSocketBroadcaster broadcaster;

    public GameWebSocketConfig(
            WebSocketSessionRegistry sessionRegistry,
            JwtHandshakeInterceptor jwtHandshakeInterceptor,
            GetGameStateUseCase getGameStateUseCase,
            GameStateWebSocketBroadcaster broadcaster) {
        this.sessionRegistry = sessionRegistry;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
        this.getGameStateUseCase = getGameStateUseCase;
        this.broadcaster = broadcaster;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(
                        new GameWebSocketHandler(sessionRegistry, getGameStateUseCase, broadcaster),
                        "/ws/games/{gameId}")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
