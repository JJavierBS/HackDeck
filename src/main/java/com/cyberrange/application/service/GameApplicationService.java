package com.cyberrange.application.service;

import com.cyberrange.application.exception.AccessDeniedException;
import com.cyberrange.application.port.in.CreateGameUseCase;
import com.cyberrange.application.port.in.EnqueueActionCommand;
import com.cyberrange.application.port.in.EnqueueActionUseCase;
import com.cyberrange.application.port.in.GameAccess;
import com.cyberrange.application.port.in.GetGameStateUseCase;
import com.cyberrange.application.port.in.JoinGameUseCase;
import com.cyberrange.application.port.in.LaunchTwistUseCase;
import com.cyberrange.application.port.in.MarkReadyUseCase;
import com.cyberrange.application.port.in.ResolveExpiredRoundsUseCase;
import com.cyberrange.application.port.in.RoundControlUseCase;
import com.cyberrange.application.port.in.ResolveRoundUseCase;
import com.cyberrange.application.port.in.StartGameUseCase;
import com.cyberrange.application.port.out.AccessTokenPort;
import com.cyberrange.application.port.out.GameRepository;
import com.cyberrange.application.port.out.GameStateBroadcaster;
import com.cyberrange.domain.exception.GameNotFoundException;
import com.cyberrange.domain.exception.GameNotJoinableException;
import com.cyberrange.domain.catalog.ActionCard;
import com.cyberrange.domain.model.ActionIntent;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.GameSettings;
import com.cyberrange.domain.model.JoinCode;
import com.cyberrange.domain.model.Participant;
import com.cyberrange.domain.model.ParticipantSession;
import com.cyberrange.domain.model.Role;
import com.cyberrange.domain.model.TeamId;
import com.cyberrange.domain.rules.RoundResolution;
import com.cyberrange.domain.rules.RuleEngine;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Orquesta los casos de uso apoyandose en el dominio y en los puertos de
 * salida. No contiene reglas de juego (eso vive en RuleEngine);
 * solo coordina persistencia, motor de reglas, credenciales y difusion.
 */
@Service
public final class GameApplicationService implements
        CreateGameUseCase, JoinGameUseCase, StartGameUseCase, LaunchTwistUseCase,
        EnqueueActionUseCase, ResolveRoundUseCase, GetGameStateUseCase,
        MarkReadyUseCase, RoundControlUseCase, ResolveExpiredRoundsUseCase {

    private static final int MAX_JOIN_CODE_ATTEMPTS = 100;

    private final GameRepository gameRepository;
    private final GameStateBroadcaster broadcaster;
    private final RuleEngine ruleEngine;
    private final AccessTokenPort accessTokenPort;

    public GameApplicationService(
            GameRepository gameRepository,
            GameStateBroadcaster broadcaster,
            RuleEngine ruleEngine,
            AccessTokenPort accessTokenPort) {
        this.gameRepository = Objects.requireNonNull(gameRepository, "gameRepository");
        this.broadcaster = Objects.requireNonNull(broadcaster, "broadcaster");
        this.ruleEngine = Objects.requireNonNull(ruleEngine, "ruleEngine");
        this.accessTokenPort = Objects.requireNonNull(accessTokenPort, "accessTokenPort");
    }

    @Override
    public GameAccess createGame(GameSettings settings) {
        Participant instructor = Participant.instructor();
        Game game = Game.create(uniqueJoinCode(), instructor, settings);
        gameRepository.save(game);
        return accessFor(game, instructor);
    }

    @Override
    public GameAccess joinGame(JoinCode joinCode, String displayName) {
        Game game = gameRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new GameNotFoundException(joinCode.toString()));
        TeamId team = game.firstFreeTeam()
                .orElseThrow(() -> new GameNotJoinableException("La partida ya tiene los dos equipos"));
        Participant player = Participant.player(team, displayName);
        game.join(player);
        gameRepository.save(game);
        broadcaster.broadcastState(game.id(), game);
        return accessFor(game, player);
    }

    @Override
    public void startGame(GameId gameId, ParticipantSession session) {
        Game game = requireGame(gameId);
        requireInstructor(game, session);
        game.startMatch();
        gameRepository.save(game);
        broadcaster.broadcastState(game.id(), game);
    }

    @Override
    public void enqueueAction(GameId gameId, ParticipantSession session, EnqueueActionCommand command) {
        Game game = requireGame(gameId);
        Participant participant = requireParticipant(game, session);
        if (participant.isInstructor()) {
            throw new AccessDeniedException("El instructor arbitra, no encola acciones");
        }
        Role side = game.sideOf(participant.team());
        ActionCard card = ruleEngine.cardFor(side, command.cardId());
        ActionIntent action = new ActionIntent(UUID.randomUUID(), side, card.id(), command.parameters());
        game.enqueue(action, ruleEngine.costOf(game, card));
        gameRepository.save(game);
        // Los turnos son simultaneos a ciegas: encolar no difunde nada, o el
        // rival veria lo que le viene encima antes de resolver la ronda.
    }

    @Override
    public void launchTwist(GameId gameId, ParticipantSession session, String cardId) {
        Game game = requireGame(gameId);
        requireInstructor(game, session);
        game.requireInProgress();
        ActionCard twist = ruleEngine.twistFor(cardId);
        game.launchTwist(twist.id(), twist.rounds(), ruleEngine.twistBudgetChange(game, twist));
        gameRepository.save(game);
        broadcaster.broadcastState(game.id(), game);
    }

    @Override
    public Game resolveCurrentRound(GameId gameId, ParticipantSession session) {
        Game game = requireGame(gameId);
        requireInstructor(game, session);
        return resolveRound(game);
    }

    @Override
    public void markReady(GameId gameId, ParticipantSession session) {
        Game game = requireGame(gameId);
        Participant participant = requireParticipant(game, session);
        if (participant.isInstructor()) {
            throw new AccessDeniedException("El instructor arbitra, no declara listo a nadie");
        }
        game.markReady(participant.team());
        // Con el modo automatico apagado esto solo avisa al instructor: es el
        // quien decide cuando se cierra la ronda.
        if (game.isAutoResolve() && game.currentRound().everyoneReady()) {
            resolveRound(game);
            return;
        }
        gameRepository.save(game);
        broadcaster.broadcastState(game.id(), game);
    }

    @Override
    public void setAutoResolve(GameId gameId, ParticipantSession session, boolean enabled) {
        Game game = requireGame(gameId);
        requireInstructor(game, session);
        game.setAutoResolve(enabled);
        // Si los dos ya habian confirmado, encender el modo automatico cierra
        // la ronda en ese momento: es lo que el instructor espera al pulsarlo.
        if (enabled && game.currentRound().everyoneReady()) {
            resolveRound(game);
            return;
        }
        gameRepository.save(game);
        broadcaster.broadcastState(game.id(), game);
    }

    @Override
    public void closeHalf(GameId gameId, ParticipantSession session) {
        Game game = requireGame(gameId);
        requireInstructor(game, session);
        game.closeHalf();
        finishIfOver(game);
        gameRepository.save(game);
        broadcaster.broadcastState(game.id(), game);
    }

    @Override
    public void closeMatch(GameId gameId, ParticipantSession session) {
        Game game = requireGame(gameId);
        requireInstructor(game, session);
        game.closeMatch();
        finishIfOver(game);
        gameRepository.save(game);
        broadcaster.broadcastState(game.id(), game);
    }

    @Override
    public int resolveExpiredRounds() {
        Instant now = Instant.now();
        int resolved = 0;
        for (Game game : gameRepository.findInProgress()) {
            if (game.isAutoResolve() && game.isRoundExpired(now)) {
                resolveRound(game);
                resolved++;
            }
        }
        return resolved;
    }

    private Game resolveRound(Game game) {
        game.requireInProgress();

        RoundResolution resolution = ruleEngine.resolveRound(game, game.currentRound());
        game.applyRoundResolution(
                resolution.resultingState(),
                resolution.generatedEvents(),
                resolution.takedown(),
                resolution.catchUpBonus());
        finishIfOver(game);

        gameRepository.save(game);
        broadcaster.broadcastState(game.id(), game);
        return game;
    }

    private void finishIfOver(Game game) {
        if (game.isOver() && game.result() == null) {
            game.recordResult(ruleEngine.scoreMatch(game));
        }
    }

    @Override
    public Game getGameState(GameId gameId, ParticipantSession session) {
        Game game = requireGame(gameId);
        requireParticipant(game, session);
        return game;
    }

    private GameAccess accessFor(Game game, Participant participant) {
        ParticipantSession session = new ParticipantSession(game.id(), participant);
        return new GameAccess(game.id(), game.joinCode(), participant, accessTokenPort.issue(session));
    }

    private Game requireGame(GameId gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId.toString()));
    }

    /**
     * Que el token sea valido no basta: tiene que ser de esta partida y ese
     * participante debe seguir siendo quien dice ser dentro de ella.
     */
    private Participant requireParticipant(Game game, ParticipantSession session) {
        if (!session.belongsTo(game.id())) {
            throw new AccessDeniedException("El token pertenece a otra partida");
        }
        Participant claimed = session.participant();
        Participant actual = claimed.isInstructor()
                ? game.instructor()
                : game.playerOf(claimed.team()).orElse(null);
        if (actual == null || !actual.id().equals(claimed.id())) {
            throw new AccessDeniedException("El participante ya no forma parte de la partida");
        }
        return actual;
    }

    private void requireInstructor(Game game, ParticipantSession session) {
        if (!requireParticipant(game, session).isInstructor()) {
            throw new AccessDeniedException("Solo el instructor puede hacer esto");
        }
    }

    private JoinCode uniqueJoinCode() {
        for (int attempt = 0; attempt < MAX_JOIN_CODE_ATTEMPTS; attempt++) {
            JoinCode candidate = JoinCode.generate();
            if (!gameRepository.existsByJoinCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No se ha podido generar un codigo de partida libre");
    }
}
