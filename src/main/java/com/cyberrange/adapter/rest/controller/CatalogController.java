package com.cyberrange.adapter.rest.controller;

import com.cyberrange.application.port.in.GetGameStateUseCase;
import com.cyberrange.application.port.out.ActionCatalogPort;
import com.cyberrange.domain.catalog.ActionCard;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.ParticipantSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/** Cada bando recibe solo su mitad del catalogo; el instructor, todo. */
@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final ActionCatalogPort catalogPort;
    private final GetGameStateUseCase getGameStateUseCase;

    public CatalogController(ActionCatalogPort catalogPort, GetGameStateUseCase getGameStateUseCase) {
        this.catalogPort = catalogPort;
        this.getGameStateUseCase = getGameStateUseCase;
    }

    @GetMapping
    public List<ActionCard> catalogFor(ParticipantSession session) {
        Game game = getGameStateUseCase.getGameState(session.gameId(), session);
        if (session.isInstructor()) {
            return catalogPort.catalog().all();
        }
        // El bando lo dice la mitad en curso, no el cliente: al cambiar de
        // mitad el mismo equipo recibe el catalogo contrario.
        List<ActionCard> cards = new ArrayList<>(
                catalogPort.catalog().playableBy(game.sideOf(session.participant().team())));
        return cards;
    }
}
