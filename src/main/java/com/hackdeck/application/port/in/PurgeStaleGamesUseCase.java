package com.hackdeck.application.port.in;

public interface PurgeStaleGamesUseCase {

    /** Borra lo caducado y devuelve cuantas partidas se han llevado por delante. */
    int purgeStaleGames();
}
