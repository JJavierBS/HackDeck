package com.cyberrange.apoyo;

import com.cyberrange.domain.catalog.ActionCard;
import com.cyberrange.domain.catalog.ActionCatalog;
import com.cyberrange.domain.model.ActionIntent;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameSettings;
import com.cyberrange.domain.model.JoinCode;
import com.cyberrange.domain.model.Participant;
import com.cyberrange.domain.model.Role;
import com.cyberrange.domain.model.TeamId;

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
