package com.hackdeck.application.limits;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Cuantas partidas vivas puede tener a la vez un mismo creador.
 *
 * El cupo por minuto no basta: limita el ritmo, no el acumulado. Con diez
 * creaciones por minuto y media hora de vida por lobby, una sola direccion
 * sostiene trescientas partidas, que es la mayor parte del tope del servidor;
 * dos direcciones lo llenan y todos los demas empiezan a recibir 503.
 *
 * Lo que cuenta son las vivas, asi que al preguntar se olvidan las que ya no
 * estan en vez de llevar la cuenta a mano desde el barrendero: si se
 * desincronizara, el error seria dejar a alguien sin poder crear nunca mas.
 */
public final class CreationQuota<ID> {

    private final int max;
    private final Map<String, Set<ID>> porCreador = new ConcurrentHashMap<>();

    public CreationQuota(int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("El cupo por creador debe ser mayor que cero");
        }
        this.max = max;
    }

    public int max() {
        return max;
    }

    public boolean hasRoom(String creador, Predicate<ID> sigueViva) {
        return liveCount(creador, sigueViva) < max;
    }

    /**
     * Se anota despues de crear, no antes, porque el identificador no existe
     * hasta entonces. Dos peticiones simultaneas del mismo creador pueden
     * colarse por ese hueco: para un limite de abuso, pasarse por uno da igual.
     */
    public void register(String creador, ID id) {
        porCreador.computeIfAbsent(creador, ignorado -> ConcurrentHashMap.newKeySet()).add(id);
    }

    public int liveCount(String creador, Predicate<ID> sigueViva) {
        Set<ID> anotadas = porCreador.get(creador);
        if (anotadas == null) {
            return 0;
        }
        anotadas.removeIf(id -> !sigueViva.test(id));
        if (anotadas.isEmpty()) {
            // Sin esto el mapa acumularia una entrada vacia por cada direccion
            // que haya creado algo alguna vez.
            porCreador.remove(creador, anotadas);
            return 0;
        }
        return anotadas.size();
    }

    /** Solo para pruebas y diagnostico: cuantos creadores se estan siguiendo. */
    public Set<String> trackedCreators() {
        return new LinkedHashSet<>(porCreador.keySet());
    }
}
