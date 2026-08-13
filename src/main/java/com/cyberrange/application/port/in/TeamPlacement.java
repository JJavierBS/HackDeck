package com.cyberrange.application.port.in;

/**
 * @param status   WAITING mientras no le toque mesa, PLAYING con mesa
 *                 asignada, ELIMINATED si ya perdio y CHAMPION si gano todo.
 * @param gameId   mesa en la que juega ahora, si la hay.
 * @param gameToken credencial para esa mesa, que el cliente usa como
 *                  siempre: para el equipo nada cambia al cambiar de mesa.
 */
public record TeamPlacement(String status, String gameId, String gameToken, int roundNumber) {

    public static TeamPlacement waiting(int roundNumber) {
        return new TeamPlacement("WAITING", null, null, roundNumber);
    }

    public static TeamPlacement playing(String gameId, String gameToken, int roundNumber) {
        return new TeamPlacement("PLAYING", gameId, gameToken, roundNumber);
    }

    public static TeamPlacement out(String status, int roundNumber) {
        return new TeamPlacement(status, null, null, roundNumber);
    }
}
