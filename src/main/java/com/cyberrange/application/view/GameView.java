package com.cyberrange.application.view;

import java.util.List;
import java.util.Map;

/**
 * Lo que un participante concreto puede ver de la partida. Los campos que
 * su rol no debe conocer viajan a null: no se trata de que el cliente los
 * oculte, es que nunca salen del servidor.
 *
 * @param ciaLevels         valores exactos de la triada; solo instructor y defensor.
 * @param ciaStatus         lectura cualitativa de la triada; lo que ve el atacante.
 * @param budgets           presupuesto de los dos equipos; solo instructor.
 * @param yourKillChain     fases que el atacante lleva desbloqueadas.
 * @param yourActiveCards   capas y persistencias propias que siguen en pie.
 * @param yourQueuedActions lo que uno mismo lleva encolado esta ronda.
 * @param events            eventos que ese rol ha llegado a percibir.
 */
public record GameView(
        String gameId,
        String joinCode,
        String phase,
        Integer halfNumber,
        int currentRoundNumber,
        int roundsPerHalf,
        long roundTimeoutSeconds,
        Map<String, String> teams,
        String yourTeam,
        String yourSide,
        Integer yourBudget,
        Map<String, Integer> budgets,
        Map<String, Integer> ciaLevels,
        Map<String, String> ciaStatus,
        List<String> yourKillChain,
        List<ActiveCardView> yourActiveCards,
        List<QueuedActionView> yourQueuedActions,
        List<EventView> events,
        MatchResultView result) {
}
