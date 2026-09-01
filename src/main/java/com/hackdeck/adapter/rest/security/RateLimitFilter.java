package com.hackdeck.adapter.rest.security;

import com.hackdeck.adapter.rest.dto.ApiErrorResponse;
import com.hackdeck.application.limits.LimitsProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

/**
 * Cupo por IP en los tres endpoints que se pueden llamar sin token: crear
 * partida, crear torneo y unirse. Los demas ya exigen un JWT emitido por el
 * servidor, asi que para llegar a ellos hay que haber pasado antes por aqui.
 *
 * El cupo de union es ademas lo que hace inviable probar codigos a ciegas:
 * el espacio es de 32^6, pero sin freno un bot los recorre igualmente.
 *
 * Cuenta por request.getRemoteAddr(), que detras del proxy inverso es ya la
 * IP real gracias a forward-headers-strategy del perfil cloud. Sin proxy
 * delante NO se debe mirar X-Forwarded-For: lo pone quien quiera.
 */
@Component
@Order(10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String JOIN_PATH = "/api/v1/games/join";
    private static final String GAMES_PATH = "/api/v1/games";
    private static final String TOURNAMENTS_PATH = "/api/v1/tournaments";

    private final TokenBucketRateLimiter joins;
    private final TokenBucketRateLimiter creations;
    private final ObjectMapper objectMapper;
    private final long retryAfterSeconds;

    public RateLimitFilter(LimitsProperties properties, ObjectMapper objectMapper) {
        this.joins = new TokenBucketRateLimiter(properties.joinAttemptsPerIp(), properties.rateLimitWindow());
        this.creations = new TokenBucketRateLimiter(properties.createAttemptsPerIp(), properties.rateLimitWindow());
        this.objectMapper = objectMapper;
        this.retryAfterSeconds = Math.max(1, properties.rateLimitWindow().toSeconds());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return limiterFor(request) == null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        TokenBucketRateLimiter limiter = limiterFor(request);
        if (limiter != null && !limiter.tryAcquire(request.getRemoteAddr(), Instant.now())) {
            reject(response);
            return;
        }
        chain.doFilter(request, response);
    }

    @Scheduled(fixedDelayString = "${hackdeck.limits.purge-interval:1m}")
    public void evictIdleBuckets() {
        Instant now = Instant.now();
        joins.evictIdle(now);
        creations.evictIdle(now);
    }

    private TokenBucketRateLimiter limiterFor(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return null;
        }
        String path = request.getRequestURI();
        if (JOIN_PATH.equals(path)) {
            return joins;
        }
        // Solo la creacion, no las acciones sobre una partida ya existente,
        // que van por /api/v1/games/{id}/... y ya llevan token.
        if (GAMES_PATH.equals(path) || TOURNAMENTS_PATH.equals(path)) {
            return creations;
        }
        return null;
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.getWriter().write(objectMapper.writeValueAsString(
                new ApiErrorResponse("demasiadas_peticiones", "Demasiadas peticiones desde esta direccion")));
    }
}
