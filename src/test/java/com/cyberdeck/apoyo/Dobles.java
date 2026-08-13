package com.cyberdeck.apoyo;

import com.cyberdeck.application.port.out.AccessTokenPort;
import com.cyberdeck.application.port.out.GameRepository;
import com.cyberdeck.application.port.out.GameStateBroadcaster;
import com.cyberdeck.domain.model.Game;
import com.cyberdeck.domain.model.GameId;
import com.cyberdeck.domain.model.GamePhase;
import com.cyberdeck.domain.model.JoinCode;
import com.cyberdeck.domain.model.ParticipantSession;
import com.cyberdeck.domain.model.TournamentSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Dobles {

    private Dobles() {
    }

    public static final class Repositorio implements GameRepository {

        private final Map<GameId, Game> partidas = new HashMap<>();

        @Override
        public Game save(Game game) {
            partidas.put(game.id(), game);
            return game;
        }

        @Override
        public Optional<Game> findById(GameId gameId) {
            return Optional.ofNullable(partidas.get(gameId));
        }

        @Override
        public Optional<Game> findByJoinCode(JoinCode joinCode) {
            return partidas.values().stream().filter(game -> game.joinCode().equals(joinCode)).findFirst();
        }

        @Override
        public boolean existsByJoinCode(JoinCode joinCode) {
            return findByJoinCode(joinCode).isPresent();
        }

        @Override
        public List<Game> findInProgress() {
            return partidas.values().stream().filter(game -> game.phase() == GamePhase.IN_PROGRESS).toList();
        }
    }

    /** Apunta cuantas veces se ha difundido, para comprobar quien no difunde. */
    public static final class Difusor implements GameStateBroadcaster {

        private final List<GameId> difusiones = new ArrayList<>();

        @Override
        public void broadcastState(GameId gameId, Game game) {
            difusiones.add(gameId);
        }

        public int veces() {
            return difusiones.size();
        }
    }

    public static final class Tokens implements AccessTokenPort {

        @Override
        public String issue(ParticipantSession session) {
            return "token-de-" + session.participant().id();
        }

        @Override
        public Optional<ParticipantSession> verify(String token) {
            return Optional.empty();
        }

        @Override
        public String issueTournament(TournamentSession session) {
            return "token-de-torneo-" + session.teamId();
        }

        @Override
        public Optional<TournamentSession> verifyTournament(String token) {
            return Optional.empty();
        }
    }
}
