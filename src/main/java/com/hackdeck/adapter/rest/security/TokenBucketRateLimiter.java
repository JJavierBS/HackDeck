package com.hackdeck.adapter.rest.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cubo de fichas por clave (aqui, por IP). Se rellena de forma continua, no
 * por ventanas fijas: asi una rafaga justo antes y justo despues del cambio
 * de ventana no cuela el doble del cupo.
 *
 * Escrito a mano y no con una libreria porque el proceso es uno solo y no
 * hay nada que compartir entre nodos; si algun dia se despliega en varios,
 * esto hay que subirlo al proxy.
 */
final class TokenBucketRateLimiter {

    /** Pasado este tiempo sin usarse, el cubo se tira: el limitador tambien puede tener fugas. */
    private static final Duration IDLE_EVICTION = Duration.ofMinutes(30);

    private final int capacity;
    private final Duration refillPeriod;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    TokenBucketRateLimiter(int capacity, Duration refillPeriod) {
        this.capacity = capacity;
        this.refillPeriod = refillPeriod;
    }

    boolean tryAcquire(String key, Instant now) {
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket(capacity, now));
        synchronized (bucket) {
            bucket.refill(now, capacity, refillPeriod);
            return bucket.consume(now);
        }
    }

    void evictIdle(Instant now) {
        buckets.entrySet().removeIf(entry -> {
            Bucket bucket = entry.getValue();
            synchronized (bucket) {
                return Duration.between(bucket.lastSeen, now).compareTo(IDLE_EVICTION) > 0;
            }
        });
    }

    int trackedKeys() {
        return buckets.size();
    }

    private static final class Bucket {

        private double tokens;
        private Instant lastRefill;
        private Instant lastSeen;

        private Bucket(int capacity, Instant now) {
            this.tokens = capacity;
            this.lastRefill = now;
            this.lastSeen = now;
        }

        private void refill(Instant now, int capacity, Duration refillPeriod) {
            long elapsed = Duration.between(lastRefill, now).toMillis();
            if (elapsed <= 0) {
                return;
            }
            double ganadas = (double) capacity * elapsed / refillPeriod.toMillis();
            tokens = Math.min(capacity, tokens + ganadas);
            lastRefill = now;
        }

        private boolean consume(Instant now) {
            lastSeen = now;
            if (tokens < 1.0) {
                return false;
            }
            tokens -= 1.0;
            return true;
        }
    }
}
