package com.cyberdeck.adapter.rest.controller;

import com.cyberdeck.adapter.rest.dto.CreateGameRequest;
import com.cyberdeck.adapter.rest.dto.CreateGameResponse;
import com.cyberdeck.adapter.rest.dto.EnqueueActionRequest;
import com.cyberdeck.adapter.rest.dto.JoinGameRequest;
import com.cyberdeck.adapter.rest.dto.JoinResponse;
import com.cyberdeck.adapter.security.InstructorAccessGuard;
import com.cyberdeck.application.port.in.CreateGameUseCase;
import com.cyberdeck.application.port.in.EnqueueActionCommand;
import com.cyberdeck.application.port.in.EnqueueActionUseCase;
import com.cyberdeck.application.port.in.GameAccess;
import com.cyberdeck.application.port.in.GetGameStateUseCase;
import com.cyberdeck.application.port.in.JoinGameUseCase;
import com.cyberdeck.application.port.in.LaunchTwistUseCase;
import com.cyberdeck.application.port.in.MarkReadyUseCase;
import com.cyberdeck.application.port.in.RoundControlUseCase;
import com.cyberdeck.application.port.in.ResolveRoundUseCase;
import com.cyberdeck.application.port.in.StartGameUseCase;
import com.cyberdeck.application.port.in.TournamentUseCase;
import com.cyberdeck.application.service.GameViewProjector;
import com.cyberdeck.application.view.GameView;
import com.cyberdeck.application.view.MatchHistoryView;
import com.cyberdeck.application.exception.AccessDeniedException;
import com.cyberdeck.domain.model.Game;
import com.cyberdeck.domain.model.GameId;
import com.cyberdeck.domain.model.JoinCode;
import com.cyberdeck.domain.model.ParticipantSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final CreateGameUseCase createGameUseCase;
    private final JoinGameUseCase joinGameUseCase;
    private final StartGameUseCase startGameUseCase;
    private final EnqueueActionUseCase enqueueActionUseCase;
    private final ResolveRoundUseCase resolveRoundUseCase;
    private final GetGameStateUseCase getGameStateUseCase;
    private final InstructorAccessGuard instructorAccessGuard;
    private final LaunchTwistUseCase launchTwistUseCase;
    private final MarkReadyUseCase markReadyUseCase;
    private final RoundControlUseCase roundControlUseCase;
    private final TournamentUseCase tournaments;
    private final GameViewProjector projector;

    public GameController(
            CreateGameUseCase createGameUseCase,
            JoinGameUseCase joinGameUseCase,
            StartGameUseCase startGameUseCase,
            EnqueueActionUseCase enqueueActionUseCase,
            ResolveRoundUseCase resolveRoundUseCase,
            GetGameStateUseCase getGameStateUseCase,
            InstructorAccessGuard instructorAccessGuard,
            LaunchTwistUseCase launchTwistUseCase,
            MarkReadyUseCase markReadyUseCase,
            RoundControlUseCase roundControlUseCase,
            TournamentUseCase tournaments,
            GameViewProjector projector) {
        this.createGameUseCase = createGameUseCase;
        this.joinGameUseCase = joinGameUseCase;
        this.startGameUseCase = startGameUseCase;
        this.enqueueActionUseCase = enqueueActionUseCase;
        this.resolveRoundUseCase = resolveRoundUseCase;
        this.getGameStateUseCase = getGameStateUseCase;
        this.instructorAccessGuard = instructorAccessGuard;
        this.launchTwistUseCase = launchTwistUseCase;
        this.markReadyUseCase = markReadyUseCase;
        this.roundControlUseCase = roundControlUseCase;
        this.tournaments = tournaments;
        this.projector = projector;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGameResponse createGame(
            HttpServletRequest request,
            @RequestBody(required = false) CreateGameRequest settingsRequest) {
        instructorAccessGuard.requireInstructorAccess(request);
        CreateGameRequest settings = settingsRequest == null
                ? new CreateGameRequest(null, null, null, null)
                : settingsRequest;
        return CreateGameResponse.from(createGameUseCase.createGame(settings.toSettings()));
    }

    /**
     * El equipo teclea un codigo y no sabe si es de una partida suelta o de
     * un torneo: se prueba primero como partida y si no, como torneo.
     */
    @PostMapping("/join")
    public JoinResponse joinGame(@RequestBody JoinGameRequest request) {
        JoinCode codigo = JoinCode.of(request.code());
        return tournaments.joinTournament(codigo, request.displayName())
                .map(JoinResponse::of)
                .orElseGet(() -> JoinResponse.of(joinGameUseCase.joinGame(codigo, request.displayName())));
    }

    @PostMapping("/{gameId}/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void startGame(@PathVariable String gameId, ParticipantSession session) {
        startGameUseCase.startGame(GameId.of(gameId), session);
    }

    @GetMapping("/{gameId}")
    public GameView getGameState(@PathVariable String gameId, ParticipantSession session) {
        Game game = getGameStateUseCase.getGameState(GameId.of(gameId), session);
        return projector.project(game, session.participant());
    }

    /**
     * Linea temporal completa del match. Solo el instructor: es material de
     * debriefing, no de partida.
     */
    @GetMapping("/{gameId}/history")
    public MatchHistoryView getHistory(@PathVariable String gameId, ParticipantSession session) {
        Game game = getGameStateUseCase.getGameState(GameId.of(gameId), session);
        if (!session.isInstructor()) {
            throw new AccessDeniedException("El historial completo es solo para el instructor");
        }
        return projector.history(game);
    }

    @PostMapping("/{gameId}/actions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void enqueueAction(
            @PathVariable String gameId,
            ParticipantSession session,
            @RequestBody EnqueueActionRequest request) {
        enqueueActionUseCase.enqueueAction(
                GameId.of(gameId),
                session,
                new EnqueueActionCommand(request.cardId(), request.parameters()));
    }

    @PostMapping("/{gameId}/twists/{cardId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void launchTwist(
            @PathVariable String gameId,
            @PathVariable String cardId,
            ParticipantSession session) {
        launchTwistUseCase.launchTwist(GameId.of(gameId), session, cardId);
    }

    @PostMapping("/{gameId}/rounds/ready")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void markReady(@PathVariable String gameId, ParticipantSession session) {
        markReadyUseCase.markReady(GameId.of(gameId), session);
    }

    @PostMapping("/{gameId}/auto-resolve/{enabled}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void setAutoResolve(
            @PathVariable String gameId, @PathVariable boolean enabled, ParticipantSession session) {
        roundControlUseCase.setAutoResolve(GameId.of(gameId), session, enabled);
    }

    @PostMapping("/{gameId}/half/close")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void closeHalf(@PathVariable String gameId, ParticipantSession session) {
        roundControlUseCase.closeHalf(GameId.of(gameId), session);
    }

    @PostMapping("/{gameId}/close")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void closeMatch(@PathVariable String gameId, ParticipantSession session) {
        roundControlUseCase.closeMatch(GameId.of(gameId), session);
    }

    @PostMapping("/{gameId}/rounds/resolve")
    public GameView resolveRound(@PathVariable String gameId, ParticipantSession session) {
        Game game = resolveRoundUseCase.resolveCurrentRound(GameId.of(gameId), session);
        return projector.project(game, session.participant());
    }
}
