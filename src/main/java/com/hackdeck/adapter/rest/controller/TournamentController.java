package com.hackdeck.adapter.rest.controller;

import com.hackdeck.adapter.rest.dto.CreateGameRequest;
import com.hackdeck.adapter.rest.dto.TournamentAccessResponse;
import com.hackdeck.adapter.security.InstructorAccessGuard;
import com.hackdeck.application.port.in.TeamPlacement;
import com.hackdeck.application.port.in.TournamentUseCase;
import com.hackdeck.application.view.TournamentView;
import com.hackdeck.domain.model.ParticipantSession;
import com.hackdeck.domain.model.TournamentId;
import com.hackdeck.domain.model.TournamentSession;
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
@RequestMapping("/api/v1/tournaments")
public class TournamentController {

    private final TournamentUseCase tournaments;
    private final InstructorAccessGuard instructorAccessGuard;

    public TournamentController(TournamentUseCase tournaments, InstructorAccessGuard instructorAccessGuard) {
        this.tournaments = tournaments;
        this.instructorAccessGuard = instructorAccessGuard;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentAccessResponse createTournament(
            HttpServletRequest request, @RequestBody(required = false) CreateGameRequest ajustes) {
        instructorAccessGuard.requireInstructorAccess(request);
        CreateGameRequest peticion = ajustes == null ? new CreateGameRequest(null, null, null, null) : ajustes;
        return TournamentAccessResponse.from(tournaments.createTournament(peticion.toSettings()));
    }

    @GetMapping("/{tournamentId}")
    public TournamentView view(@PathVariable String tournamentId, ParticipantSession session) {
        return tournaments.view(TournamentId.of(tournamentId), session);
    }

    @PostMapping("/{tournamentId}/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void start(@PathVariable String tournamentId, ParticipantSession session) {
        tournaments.startTournament(TournamentId.of(tournamentId), session);
    }

    @PostMapping("/{tournamentId}/rounds/next")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void nextRound(@PathVariable String tournamentId, ParticipantSession session) {
        tournaments.startNextRound(TournamentId.of(tournamentId), session);
    }

    /** Donde juega ahora el equipo que pregunta. */
    @GetMapping("/me")
    public TeamPlacement placement(TournamentSession session) {
        return tournaments.placementOf(session);
    }
}
