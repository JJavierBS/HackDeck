package com.cyberdeck.application.port.in;

import com.cyberdeck.domain.model.GameSettings;
import com.cyberdeck.domain.model.JoinCode;
import com.cyberdeck.domain.model.ParticipantSession;
import com.cyberdeck.domain.model.TournamentId;
import com.cyberdeck.domain.model.TournamentSession;

public interface TournamentUseCase {

    TournamentAccess createTournament(GameSettings settings);

    /** Devuelve vacio si ese codigo no es de un torneo. */
    java.util.Optional<TournamentAccess> joinTournament(JoinCode joinCode, String displayName);

    void startTournament(TournamentId id, ParticipantSession session);

    /** Empareja a los ganadores y siembra las mesas de la ronda siguiente. */
    void startNextRound(TournamentId id, ParticipantSession session);

    /**
     * Donde juega ahora este equipo. Es lo que hace transparente el cambio de
     * mesa: el cliente pregunta y recibe el token de la mesa que le toque.
     */
    TeamPlacement placementOf(TournamentSession session);

    com.cyberdeck.application.view.TournamentView view(TournamentId id, ParticipantSession session);
}
