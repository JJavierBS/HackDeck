package com.cyberrange.application.service;

import com.cyberrange.application.view.ActiveCardView;
import com.cyberrange.application.view.EventDetailView;
import com.cyberrange.application.view.EventView;
import com.cyberrange.application.view.GameView;
import com.cyberrange.application.view.MatchHistoryView;
import com.cyberrange.application.view.MatchResultView;
import com.cyberrange.application.view.QueuedActionView;
import com.cyberrange.domain.catalog.KillChainPhase;
import com.cyberrange.domain.model.ActionIntent;
import com.cyberrange.domain.model.ActiveCard;
import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.EventDetail;
import com.cyberrange.domain.model.GameEvent;
import com.cyberrange.domain.model.GamePhase;
import com.cyberrange.domain.model.Half;
import com.cyberrange.domain.model.MatchResult;
import com.cyberrange.domain.model.Participant;
import com.cyberrange.domain.model.Role;
import com.cyberrange.domain.model.TeamId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unico sitio donde se decide quien ve que, para el REST y para la difusion
 * por igual: dos caminos distintos acabarian filtrando cosas distintas.
 */
@Service
public final class GameViewProjector {

    public GameView project(Game game, Participant viewer) {
        boolean started = game.phase() != GamePhase.PREPARATION;
        Half half = started ? game.currentHalf() : null;
        TeamId viewerTeam = viewer.team();
        Role viewerSide = viewerTeam == null || half == null ? null : game.sideOf(viewerTeam);

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
                half == null ? null : ciaLevelsOf(half),
                half == null ? null : half.attackingTeam().name(),
                half == null ? null : game.roundDeadline().map(Instant::toString).orElse(null),
                game.isAutoResolve(),
                half == null ? List.of() : readyTeamsOf(half),
                viewer.isInstructor() && half != null ? queuedBySide(half) : null,
                half == null || viewerSide != Role.ATTACKER ? List.of() : killChainOf(half),
                half == null ? List.of() : activeCardsFor(half, viewerSide),
                half == null || viewerSide == null ? List.of() : queuedActionsOf(half, viewerSide),
                eventsFor(game, half, viewerSide),
                resultOf(game.result()));
    }

    private static Map<String, String> teamsOf(Game game) {
        Map<String, String> teams = new LinkedHashMap<>();
        for (TeamId team : TeamId.values()) {
            game.playerOf(team).ifPresent(player -> teams.put(team.name(), player.displayName()));
        }
        return teams;
    }

    private static List<String> readyTeamsOf(Half half) {
        return half.currentRound().readyTeams().stream().map(TeamId::name).sorted().toList();
    }

    /**
     * El instructor si ve las dos colas antes de resolver, porque su panel
     * ya no es lo que se proyecta: la pantalla del aula es otra.
     */
    private static Map<String, List<QueuedActionView>> queuedBySide(Half half) {
        Map<String, List<QueuedActionView>> bySide = new LinkedHashMap<>();
        for (Role side : Role.values()) {
            bySide.put(side.name(), queuedActionsOf(half, side));
        }
        return bySide;
    }

    private static List<String> killChainOf(Half half) {
        return half.unlockedPhases().stream().map(KillChainPhase::name).sorted().toList();
    }

    private static List<ActiveCardView> activeCardsFor(Half half, Role viewerSide) {
        List<ActiveCard> visible = viewerSide == null ? half.activeCards() : half.activeCardsOf(viewerSide);
        return visible.stream()
                .map(card -> new ActiveCardView(
                        card.cardId(),
                        card.side() == null ? null : card.side().name(),
                        card.isPermanent() ? null : card.roundsRemaining()))
                .toList();
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

    private static List<EventView> eventsFor(Game game, Half half, Role viewerSide) {
        return game.history().stream()
                .filter(event -> half == null || event.halfNumber() == half.number())
                .filter(event -> viewerSide == null || event.isVisibleTo(viewerSide))
                .map(event -> toEventView(adjustDetail(event, viewerSide)))
                .toList();
    }

    /**
     * Al atacante se le dice que algo le freno, pero no que carta era. De una
     * accion del rival no se ensena lo que a el le aporto.
     */
    private static GameEvent adjustDetail(GameEvent event, Role viewerSide) {
        if (event.detail() == null || viewerSide == null) {
            return event;
        }
        if (event.actor() != null && event.actor() != viewerSide) {
            return event.withDetail(event.detail().asRivalAction());
        }
        return viewerSide == Role.ATTACKER
                ? event.withDetail(event.detail().withoutCounterName())
                : event;
    }

    /**
     * Sin filtrar por rol: al acabar ya no hay niebla que proteger y la gracia
     * del debriefing es ver lo que el rival hacia sin que te enterases.
     */
    public MatchHistoryView history(Game game) {
        return new MatchHistoryView(
                game.id().toString(),
                game.joinCode().toString(),
                game.phase().name(),
                new MatchHistoryView.Settings(
                        game.settings().roundsPerHalf(),
                        game.settings().roundTimeout().toSeconds(),
                        game.settings().initialBudget(),
                        game.settings().incomePerRound()),
                teamsOf(game),
                game.history().stream().map(GameViewProjector::toEventView).toList(),
                resultOf(game.result()));
    }

    private static QueuedActionView toQueuedActionView(ActionIntent action) {
        return new QueuedActionView(action.cardId(), action.parameters());
    }

    private static EventView toEventView(GameEvent event) {
        // Siempre en el orden de la triada: Map.copyOf no conserva ninguno y
        // los pilares saldrian barajados de un evento a otro.
        Map<String, Integer> ciaAfter = new LinkedHashMap<>();
        for (CiaPillar pillar : CiaPillar.values()) {
            if (event.ciaAfter().containsKey(pillar)) {
                ciaAfter.put(pillar.name(), event.ciaAfter().get(pillar));
            }
        }
        return new EventView(
                event.halfNumber(),
                event.roundNumber(),
                event.type().name(),
                event.actor() == null ? null : event.actor().name(),
                event.cardId(),
                event.description(),
                ciaAfter,
                toDetailView(event.detail()),
                event.occurredAt().toString());
    }

    private static EventDetailView toDetailView(EventDetail detail) {
        if (detail == null) {
            return null;
        }
        Map<String, Integer> impact = new LinkedHashMap<>();
        for (CiaPillar pillar : CiaPillar.values()) {
            if (detail.impact().containsKey(pillar)) {
                impact.put(pillar.name(), detail.impact().get(pillar));
            }
        }
        return new EventDetailView(
                detail.success(),
                detail.failureReason() == null ? null : detail.failureReason().name(),
                impact,
                detail.mitigated(),
                detail.unlocked(),
                detail.boosts(),
                detail.detected(),
                detail.counteredBy());
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
