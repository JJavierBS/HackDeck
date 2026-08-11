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
 * @param attackingTeam     equipo que ataca en la mitad en curso.
 * @param roundDeadlineAt   cuando se agota el tiempo de la ronda en curso.
 * @param autoResolve       si el instructor tiene puesto el cierre automatico.
 * @param readyTeams        equipos que ya han declarado su jugada.
 * @param queuedBySide      colas de ambos bandos; solo para el instructor y
 *                          solo en su panel, nunca en la pantalla proyectada.
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
        String attackingTeam,
        String roundDeadlineAt,
        Boolean autoResolve,
        List<String> readyTeams,
        Map<String, List<QueuedActionView>> queuedBySide,
        List<String> yourKillChain,
        List<ActiveCardView> yourActiveCards,
        List<QueuedActionView> yourQueuedActions,
        List<EventView> events,
        MatchResultView result) {
}
