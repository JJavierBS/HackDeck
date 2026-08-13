package com.cyberrange.apoyo;

import com.cyberrange.application.port.out.AccessTokenPort;
import com.cyberrange.application.port.out.GameRepository;
import com.cyberrange.application.port.out.GameStateBroadcaster;
import com.cyberrange.domain.model.Game;
import com.cyberrange.domain.model.GameId;
import com.cyberrange.domain.model.GamePhase;
import com.cyberrange.domain.model.JoinCode;
import com.cyberrange.domain.model.ParticipantSession;

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
    }
}
