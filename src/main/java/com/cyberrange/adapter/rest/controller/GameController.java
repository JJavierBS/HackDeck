package com.cyberrange.adapter.rest.controller;

import com.cyberrange.adapter.rest.dto.CreateGameRequest;
import com.cyberrange.adapter.rest.dto.CreateGameResponse;
import com.cyberrange.adapter.rest.dto.EnqueueActionRequest;
import com.cyberrange.adapter.rest.dto.GameStateResponse;
import com.cyberrange.adapter.rest.dto.JoinGameRequest;
import com.cyberrange.adapter.security.InstructorAccessGuard;
import com.cyberrange.application.port.in.CreateGameUseCase;
import com.cyberrange.application.port.in.EnqueueActionCommand;
import com.cyberrange.application.port.in.EnqueueActionUseCase;
import com.cyberrange.application.port.in.GameAccess;
import com.cyberrange.application.port.in.GetGameStateUseCase;
import com.cyberrange.application.port.in.JoinGameUseCase;
import com.cyberrange.application.port.in.ResolveRoundUseCase;
import com.cyberrange.application.port.in.StartGameUseCase;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.JoinCode;
import com.cyberrange.domain.model.ParticipantSession;
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

    public GameController(
            CreateGameUseCase createGameUseCase,
            JoinGameUseCase joinGameUseCase,
            StartGameUseCase startGameUseCase,
            EnqueueActionUseCase enqueueActionUseCase,
            ResolveRoundUseCase resolveRoundUseCase,
            GetGameStateUseCase getGameStateUseCase,
            InstructorAccessGuard instructorAccessGuard) {
        this.createGameUseCase = createGameUseCase;
        this.joinGameUseCase = joinGameUseCase;
        this.startGameUseCase = startGameUseCase;
        this.enqueueActionUseCase = enqueueActionUseCase;
        this.resolveRoundUseCase = resolveRoundUseCase;
        this.getGameStateUseCase = getGameStateUseCase;
        this.instructorAccessGuard = instructorAccessGuard;
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

    @PostMapping("/join")
    public CreateGameResponse joinGame(@RequestBody JoinGameRequest request) {
        GameAccess access = joinGameUseCase.joinGame(JoinCode.of(request.code()), request.displayName());
        return CreateGameResponse.from(access);
    }

    @PostMapping("/{gameId}/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void startGame(@PathVariable String gameId, ParticipantSession session) {
        startGameUseCase.startGame(GameId.of(gameId), session);
    }

    @GetMapping("/{gameId}")
    public GameStateResponse getGameState(@PathVariable String gameId, ParticipantSession session) {
        Game game = getGameStateUseCase.getGameState(GameId.of(gameId), session);
        return GameStateResponse.from(game, session.participant());
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
                new EnqueueActionCommand(request.actionType(), request.parameters(), request.noisy()));
    }

    @PostMapping("/{gameId}/rounds/resolve")
    public GameStateResponse resolveRound(@PathVariable String gameId, ParticipantSession session) {
        Game game = resolveRoundUseCase.resolveCurrentRound(GameId.of(gameId), session);
        return GameStateResponse.from(game, session.participant());
    }
}
