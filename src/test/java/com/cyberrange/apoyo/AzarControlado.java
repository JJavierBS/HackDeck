package com.cyberrange.apoyo;

import com.cyberrange.domain.rules.Randomizer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Azar de mentira: responde lo que se le diga y apunta las probabilidades
 * que le han preguntado, que es como se comprueba que la kill chain y los
 * counters las han recortado bien.
 */
public final class AzarControlado implements Randomizer {

    private final List<Double> preguntadas = new ArrayList<>();
    private final Deque<Boolean> respuestas = new ArrayDeque<>();
    private final boolean porDefecto;

    private AzarControlado(boolean porDefecto) {
        this.porDefecto = porDefecto;
    }

    public static AzarControlado siempre() {
        return new AzarControlado(true);
    }

    public static AzarControlado nunca() {
        return new AzarControlado(false);
    }

    public AzarControlado responde(boolean... valores) {
        for (boolean valor : valores) {
            respuestas.add(valor);
        }
        return this;
    }

    @Override
    public boolean chance(double probability) {
        preguntadas.add(probability);
        return respuestas.isEmpty() ? porDefecto : respuestas.poll();
    }

    public List<Double> preguntadas() {
        return List.copyOf(preguntadas);
    }

    public double primeraPreguntada() {
        return preguntadas.getFirst();
    }
}
