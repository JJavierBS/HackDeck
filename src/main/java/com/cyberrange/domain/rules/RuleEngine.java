package com.cyberrange.domain.rules;

import com.cyberrange.domain.model.ActionIntent;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.MatchResult;
import com.cyberrange.domain.model.Round;

/**
 * Motor de reglas: aplica las acciones encoladas de una ronda sobre el
 * estado de la partida (kill chain, deteccion, catch-up, etc.), decide lo
 * que cuesta cada accion y puntua el match. Es el unico lugar donde vive la
 * logica de juego.
 */
public interface RuleEngine {

    RoundResolution resolveRound(Game game, Round round);

    int costOf(ActionIntent action);

    MatchResult scoreMatch(Game game);
}
