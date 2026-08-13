package com.hackdeck.apoyo;

import com.hackdeck.domain.catalog.ActionCard;
import com.hackdeck.domain.catalog.ActionCatalog;
import com.hackdeck.domain.model.ActionIntent;
import com.hackdeck.domain.model.Game;
import com.hackdeck.domain.model.GameSettings;
import com.hackdeck.domain.model.JoinCode;
import com.hackdeck.domain.model.Participant;
import com.hackdeck.domain.model.Role;
import com.hackdeck.domain.model.TeamId;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Partidas {

    public static final GameSettings AJUSTES = new GameSettings(3, Duration.ofSeconds(60), 100, 20);

    private Partidas() {
    }

    public static Game enCurso() {
        return enCurso(AJUSTES);
    }

    public static Game enCurso(GameSettings ajustes) {
        Game partida = enPreparacion(ajustes);
        partida.startMatch();
        return partida;
    }

    public static Game enPreparacion(GameSettings ajustes) {
        Game partida = Game.create(JoinCode.generate(), Participant.instructor(), ajustes);
        partida.join(Participant.player(TeamId.A, "Rojos"));
        partida.join(Participant.player(TeamId.B, "Azules"));
        return partida;
    }

    public static void encola(Game partida, Role bando, String cartaId) {
        partida.enqueue(new ActionIntent(UUID.randomUUID(), bando, cartaId, Map.of()), 0);
    }

    public static ActionCatalog catalogo(ActionCard... cartas) {
        return new ActionCatalog(List.of(cartas));
    }

    public static ActionCatalog catalogoVacio() {
        return new ActionCatalog(List.of());
    }
}
