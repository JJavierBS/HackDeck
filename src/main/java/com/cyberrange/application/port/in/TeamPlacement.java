package com.cyberrange.application.port.in;

/**
 * @param team      bando que le toca en esa mesa. Sin esto el cliente no
 *                  sabe si es jugador o instructor y se pinta la pantalla
 *                  equivocada.
 * @param gameToken credencial para esa mesa, que el cliente usa como
 *                  siempre: para el equipo nada cambia al cambiar de mesa.
 */
public record TeamPlacement(String status, String gameId, String gameToken, String team, int roundNumber) {

    public static TeamPlacement waiting(int roundNumber) {
        return new TeamPlacement("WAITING", null, null, null, roundNumber);
    }

    public static TeamPlacement playing(String gameId, String gameToken, String team, int roundNumber) {
        return new TeamPlacement("PLAYING", gameId, gameToken, team, roundNumber);
    }

    public static TeamPlacement out(String status, int roundNumber) {
        return new TeamPlacement(status, null, null, null, roundNumber);
    }
}
