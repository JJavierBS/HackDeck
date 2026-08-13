package com.cyberrange.domain.rules;

import com.cyberrange.domain.catalog.ActionCard;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.MatchResult;
import com.cyberrange.domain.model.Role;
import com.cyberrange.domain.model.Round;
import com.cyberrange.domain.model.TeamId;

import java.util.Map;

/**
 * Motor de reglas: valida lo que se encola, aplica las acciones de una
 * ronda sobre el estado de la partida (kill chain, counters, deteccion,
 * catch-up) y puntua el match. Es el unico lugar donde vive la logica de
 * juego.
 */
public interface RuleEngine {

    ActionCard cardFor(Role side, String cardId);

    int costOf(Game game, ActionCard card);

    ActionCard twistFor(String cardId);

    Map<TeamId, Integer> twistBudgetChange(Game game, ActionCard twist);

    RoundResolution resolveRound(Game game, Round round);

    MatchResult scoreMatch(Game game);
}
