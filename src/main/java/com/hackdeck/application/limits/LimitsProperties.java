package com.hackdeck.application.limits;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

/**
 * Cupos de un despliegue publico. En el aula sobran de largo: estan puestos
 * para que una sola IP no pueda llenar la memoria del servidor ni probar
 * codigos de partida a ciegas.
 *
 * @param maxLiveGames         partidas vivas a la vez; pasado el tope, crear
 *                             devuelve 503 en vez de agotar la memoria.
 * @param maxLiveTournaments   lo mismo para torneos.
 * @param maxLiveGamesPerIp    partidas vivas a la vez de un mismo creador. El
 *                             cupo por minuto limita el ritmo, no el
 *                             acumulado; sin esto, dos direcciones bastan para
 *                             ocupar el servidor entero.
 * @param maxLiveTournamentsPerIp  lo mismo para torneos.
 * @param lobbyTtl             sin empezar y sin actividad, se borra.
 * @param idleTtl              empezada pero abandonada a media partida.
 * @param finishedTtl          terminada; el margen es para el debriefing.
 * @param purgeInterval        cada cuanto pasa el barrendero.
 * @param joinAttemptsPerIp    intentos de union por IP y ventana. Generoso a
 *                             proposito: un instituto entero sale por una sola
 *                             IP y toda la clase se une a la vez. Aun asi deja
 *                             la fuerza bruta sobre 32^6 codigos fuera de
 *                             alcance.
 * @param createAttemptsPerIp  partidas o torneos creados por IP y ventana.
 * @param rateLimitWindow      ventana de ambos cupos.
 * @param maxSocketsPerParticipant  conexiones de tiempo real a la vez del
 *                             mismo participante. El difusor proyecta el
 *                             estado una vez por conexion, asi que abrirlas
 *                             sale mas caro al servidor que a quien las abre.
 * @param maxRequestBody       tope al cuerpo de las peticiones de la API. La
 *                             peticion mas gorda que existe es reordenar la
 *                             cola, unos pocos kilobytes, asi que 32 KB ya es
 *                             holgado.
 */
@ConfigurationProperties(prefix = "hackdeck.limits")
public record LimitsProperties(
        @DefaultValue("500") int maxLiveGames,
        @DefaultValue("100") int maxLiveTournaments,
        @DefaultValue("10") int maxLiveGamesPerIp,
        @DefaultValue("3") int maxLiveTournamentsPerIp,
        @DefaultValue("30m") Duration lobbyTtl,
        @DefaultValue("2h") Duration idleTtl,
        @DefaultValue("2h") Duration finishedTtl,
        @DefaultValue("1m") Duration purgeInterval,
        @DefaultValue("60") int joinAttemptsPerIp,
        @DefaultValue("10") int createAttemptsPerIp,
        @DefaultValue("1m") Duration rateLimitWindow,
        @DefaultValue("4") int maxSocketsPerParticipant,
        @DefaultValue("32KB") DataSize maxRequestBody) {

    public LimitsProperties {
        requirePositive(maxLiveGames, "max-live-games");
        requirePositive(maxLiveTournaments, "max-live-tournaments");
        requirePositive(maxLiveGamesPerIp, "max-live-games-per-ip");
        requirePositive(maxLiveTournamentsPerIp, "max-live-tournaments-per-ip");
        requirePositive(joinAttemptsPerIp, "join-attempts-per-ip");
        requirePositive(createAttemptsPerIp, "create-attempts-per-ip");
        requirePositive(lobbyTtl, "lobby-ttl");
        requirePositive(idleTtl, "idle-ttl");
        requirePositive(finishedTtl, "finished-ttl");
        requirePositive(purgeInterval, "purge-interval");
        requirePositive(rateLimitWindow, "rate-limit-window");
        requirePositive(maxSocketsPerParticipant, "max-sockets-per-participant");
        if (maxRequestBody == null || maxRequestBody.toBytes() <= 0) {
            throw new IllegalArgumentException("hackdeck.limits.max-request-body debe ser un tamano positivo");
        }
    }

    public RetentionPolicy retentionPolicy() {
        return new RetentionPolicy(lobbyTtl, idleTtl, finishedTtl, maxLiveGames, maxLiveTournaments);
    }

    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException("hackdeck.limits." + name + " debe ser mayor que cero");
        }
    }

    private static void requirePositive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("hackdeck.limits." + name + " debe ser una duracion positiva");
        }
    }
}
