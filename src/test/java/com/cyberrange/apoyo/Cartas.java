package com.cyberrange.apoyo;

import com.cyberrange.domain.catalog.ActionCard;
import com.cyberrange.domain.catalog.CardDuration;
import com.cyberrange.domain.catalog.CardEffect;
import com.cyberrange.domain.catalog.CardType;
import com.cyberrange.domain.catalog.DefenseCategory;
import com.cyberrange.domain.catalog.KillChainPhase;
import com.cyberrange.domain.catalog.NoiseLevel;
import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.Role;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cartas hechas a medida para cada test. No se usa el catalogo real a
 * proposito: si los tests dependieran de el, cualquier reajuste de
 * equilibrio los rompería sin que nada este mal.
 */
public final class Cartas {

    private String id = "carta";
    private CardType type = CardType.ACTION;
    private Role side = Role.ATTACKER;
    private KillChainPhase phase;
    private DefenseCategory category;
    private int cost;
    private NoiseLevel noise = NoiseLevel.NONE;
    private double successRate = 1.0;
    private CardDuration duration = CardDuration.INSTANT;
    private int rounds;
    private Map<CiaPillar, Integer> impact = Map.of();
    private int mitigation;
    private int detection;
    private Map<String, Double> counters = Map.of();
    private Set<KillChainPhase> unlocks = Set.of();
    private Map<String, Double> bonus = Map.of();
    private Set<CardEffect> effects = Set.of();

    public static Cartas ataque(String id) {
        Cartas carta = new Cartas();
        carta.id = id;
        carta.side = Role.ATTACKER;
        carta.phase = KillChainPhase.RECON;
        return carta;
    }

    public static Cartas defensa(String id) {
        Cartas carta = new Cartas();
        carta.id = id;
        carta.side = Role.DEFENDER;
        carta.category = DefenseCategory.HYGIENE;
        return carta;
    }

    public Cartas fase(KillChainPhase valor) {
        this.phase = valor;
        return this;
    }

    public Cartas cuesta(int valor) {
        this.cost = valor;
        return this;
    }

    public Cartas ruido(NoiseLevel valor) {
        this.noise = valor;
        return this;
    }

    public Cartas acierto(double valor) {
        this.successRate = valor;
        return this;
    }

    public Cartas dura(CardDuration valor, int cuantas) {
        this.duration = valor;
        this.rounds = cuantas;
        return this;
    }

    public Cartas impacto(CiaPillar pilar, int delta) {
        this.impact = Map.of(pilar, delta);
        return this;
    }

    public Cartas mitiga(int valor) {
        this.mitigation = valor;
        return this;
    }

    public Cartas detecta(int valor) {
        this.detection = valor;
        return this;
    }

    public Cartas contrarresta(String cartaRival, double loQueLeQueda) {
        this.counters = Map.of(cartaRival, loQueLeQueda);
        return this;
    }

    public Cartas desbloquea(KillChainPhase valor) {
        this.unlocks = Set.of(valor);
        return this;
    }

    public Cartas mejora(String otraCarta, double cuanto) {
        this.bonus = Map.of(otraCarta, cuanto);
        return this;
    }

    public Cartas conEfecto(CardEffect valor) {
        this.effects = Set.of(valor);
        this.type = valor == CardEffect.BUDGET_CUT ? CardType.TWIST : this.type;
        return this;
    }

    public Cartas categoria(DefenseCategory valor) {
        this.category = valor;
        return this;
    }

    public ActionCard build() {
        return new ActionCard(
                id, type, side, phase, category,
                Map.of("es", id, "en", id), Map.of("es", id, "en", id),
                cost, noise, successRate, duration, rounds,
                impact, mitigation, detection,
                counters, List.of(), unlocks, bonus, effects);
    }
}
