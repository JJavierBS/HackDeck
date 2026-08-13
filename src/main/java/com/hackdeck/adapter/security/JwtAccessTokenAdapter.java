package com.hackdeck.adapter.security;

import com.hackdeck.application.port.out.AccessTokenPort;
import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.Participant;
import com.hackdeck.domain.model.ParticipantKind;
import com.hackdeck.domain.model.ParticipantSession;
import com.hackdeck.domain.model.PlayerId;
import com.hackdeck.domain.model.TeamId;
import com.hackdeck.domain.model.TournamentId;
import com.hackdeck.domain.model.TournamentSession;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public final class JwtAccessTokenAdapter implements AccessTokenPort {

    private static final Logger log = LoggerFactory.getLogger(JwtAccessTokenAdapter.class);

    private static final String ALGORITHM = "HS256";
    private static final String CLAIM_GAME_ID = "gid";
    private static final String CLAIM_TOURNAMENT_ID = "tid";
    private static final String CLAIM_KIND = "kind";
    private static final String CLAIM_TEAM = "team";
    private static final String CLAIM_NAME = "name";

    private final SecretKey signingKey;
    private final SecurityProperties properties;

    public JwtAccessTokenAdapter(SecurityProperties properties) {
        this.properties = properties;
        this.signingKey = resolveSigningKey(properties);
    }

    @Override
    public String issue(ParticipantSession session) {
        Participant participant = session.participant();
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(participant.id().toString())
                .claim(CLAIM_GAME_ID, session.gameId().toString())
                .claim(CLAIM_KIND, participant.kind().name())
                .claim(CLAIM_TEAM, participant.team() == null ? null : participant.team().name())
                .claim(CLAIM_NAME, participant.displayName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.tokenTtl())))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String issueTournament(TournamentSession session) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(session.teamId().toString())
                .claim(CLAIM_TOURNAMENT_ID, session.tournamentId().toString())
                .claim(CLAIM_NAME, session.displayName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.tokenTtl())))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public Optional<TournamentSession> verifyTournament(String token) {
        return claimsOf(token)
                .filter(claims -> claims.get(CLAIM_TOURNAMENT_ID, String.class) != null)
                .map(claims -> new TournamentSession(
                        TournamentId.of(claims.get(CLAIM_TOURNAMENT_ID, String.class)),
                        PlayerId.of(claims.getSubject()),
                        claims.get(CLAIM_NAME, String.class)));
    }

    @Override
    public Optional<ParticipantSession> verify(String token) {
        return claimsOf(token)
                .filter(claims -> claims.get(CLAIM_GAME_ID, String.class) != null)
                .map(JwtAccessTokenAdapter::toSession);
    }

    private Optional<Claims> claimsOf(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            if (!ALGORITHM.equals(jws.getHeader().getAlgorithm())) {
                return Optional.empty();
            }
            return Optional.of(jws.getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static ParticipantSession toSession(Claims claims) {
        String team = claims.get(CLAIM_TEAM, String.class);
        Participant participant = new Participant(
                PlayerId.of(claims.getSubject()),
                ParticipantKind.valueOf(claims.get(CLAIM_KIND, String.class)),
                team == null ? null : TeamId.valueOf(team),
                claims.get(CLAIM_NAME, String.class));
        return new ParticipantSession(GameId.of(claims.get(CLAIM_GAME_ID, String.class)), participant);
    }

    private static SecretKey resolveSigningKey(SecurityProperties properties) {
        if (!properties.hasJwtSecret()) {
            log.warn("hackdeck.security.jwt-secret sin configurar: se genera una clave aleatoria. "
                    + "Los tokens dejaran de ser validos al reiniciar el backend.");
            return Jwts.SIG.HS256.key().build();
        }
        byte[] secret = properties.jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < SecurityProperties.MIN_SECRET_LENGTH) {
            throw new IllegalStateException("hackdeck.security.jwt-secret necesita al menos "
                    + SecurityProperties.MIN_SECRET_LENGTH + " caracteres");
        }
        return Keys.hmacShaKeyFor(secret);
    }
}
