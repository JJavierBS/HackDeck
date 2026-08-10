package com.cyberrange.application.service;

import com.cyberrange.application.view.EventView;
import com.cyberrange.application.view.GameView;
import com.cyberrange.application.view.MatchResultView;
import com.cyberrange.application.view.QueuedActionView;
import com.cyberrange.domain.model.ActionIntent;
import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameEvent;
import com.cyberrange.domain.model.GamePhase;
import com.cyberrange.domain.model.Half;
import com.cyberrange.domain.model.MatchResult;
import com.cyberrange.domain.model.Participant;
import com.cyberrange.domain.model.PillarStatus;
import com.cyberrange.domain.model.Role;
import com.cyberrange.domain.model.Round;
import com.cyberrange.domain.model.TeamId;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Construye la vista de la partida que corresponde a cada participante.
 * Es el unico sitio donde se decide quien ve que, tanto para el REST como
 * para la difusion en tiempo real, para que las dos vias no puedan acabar
 * filtrando cosas distintas.
 */
@Service
public final class GameViewProjector {

    public GameView project(Game game, Participant viewer) {
        boolean started = game.phase() != GamePhase.PREPARATION;
        Half half = started ? game.currentHalf() : null;
        TeamId viewerTeam = viewer.team();
        Role viewerSide = viewerTeam == null || half == null ? null : game.sideOf(viewerTeam);
        boolean seesExactCia = viewer.isInstructor() || viewerSide == Role.DEFENDER;

        return new GameView(
                game.id().toString(),
                game.joinCode().toString(),
                game.phase().name(),
                half == null ? null : half.number(),
                half == null ? 0 : half.currentRound().number(),
                game.settings().roundsPerHalf(),
                game.settings().roundTimeout().toSeconds(),
                teamsOf(game),
                viewerTeam == null ? null : viewerTeam.name(),
                viewerSide == null ? null : viewerSide.name(),
                viewerTeam == null || half == null ? null : half.budgetOf(viewerTeam),
                viewer.isInstructor() && half != null ? budgetsOf(half) : null,
                half != null && seesExactCia ? ciaLevelsOf(half) : null,
                half != null && !seesExactCia ? ciaStatusOf(half) : null,
                half == null || viewerSide == null ? List.of() : queuedActionsOf(half, viewerSide),
                half == null ? List.of() : eventsFor(half, viewerSide),
                resultOf(game.result()));
    }

    private static Map<String, String> teamsOf(Game game) {
        Map<String, String> teams = new LinkedHashMap<>();
        for (TeamId team : TeamId.values()) {
            game.playerOf(team).ifPresent(player -> teams.put(team.name(), player.displayName()));
        }
        return teams;
    }

    private static Map<String, Integer> budgetsOf(Half half) {
        Map<String, Integer> budgets = new LinkedHashMap<>();
        half.budgets().forEach((team, budget) -> budgets.put(team.name(), budget));
        return budgets;
    }

    private static Map<String, Integer> ciaLevelsOf(Half half) {
        Map<String, Integer> levels = new LinkedHashMap<>();
        for (CiaPillar pillar : CiaPillar.values()) {
            levels.put(pillar.name(), half.ciaState().levelOf(pillar));
        }
        return levels;
    }

    private static Map<String, String> ciaStatusOf(Half half) {
        Map<String, String> status = new LinkedHashMap<>();
        for (CiaPillar pillar : CiaPillar.values()) {
            status.put(pillar.name(), PillarStatus.of(half.ciaState().levelOf(pillar)).name());
        }
        return status;
    }

    /**
     * Solo las propias: los turnos son simultaneos a ciegas y ver la cola
     * del rival antes de resolver arruinaria la ronda.
     */
    private static List<QueuedActionView> queuedActionsOf(Half half, Role viewerSide) {
        return half.currentRound().queuedActions().stream()
                .filter(action -> action.team() == viewerSide)
                .map(GameViewProjector::toQueuedActionView)
                .toList();
    }

    private static List<EventView> eventsFor(Half half, Role viewerSide) {
        return half.rounds().stream()
                .map(Round::events)
                .flatMap(List::stream)
                .filter(event -> viewerSide == null || event.isVisibleTo(viewerSide))
                .map(GameViewProjector::toEventView)
                .toList();
    }

    private static QueuedActionView toQueuedActionView(ActionIntent action) {
        return new QueuedActionView(action.cardId(), action.parameters());
    }

    private static EventView toEventView(GameEvent event) {
        return new EventView(
                event.roundNumber(),
                event.actor().name(),
                event.description(),
                event.occurredAt().toString());
    }

    private static MatchResultView resultOf(MatchResult result) {
        if (result == null) {
            return null;
        }
        return new MatchResultView(
                result.winner() == null ? null : result.winner().name(),
                result.outcome().name(),
                byTeamName(result.defendedCia()),
                byTeamName(result.takedownRound()));
    }

    private static Map<String, Integer> byTeamName(Map<TeamId, Integer> values) {
        Map<String, Integer> byName = new LinkedHashMap<>();
        values.forEach((team, value) -> byName.put(team.name(), value));
        return byName;
    }
}
