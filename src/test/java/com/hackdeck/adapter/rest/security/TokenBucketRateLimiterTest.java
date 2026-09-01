package com.hackdeck.adapter.rest.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketRateLimiterTest {

    private static final Instant AHORA = Instant.parse("2026-08-31T10:00:00Z");

    @Test
    void deja_pasar_hasta_el_cupo_y_corta_el_siguiente() {
        TokenBucketRateLimiter limitador = new TokenBucketRateLimiter(3, Duration.ofMinutes(1));

        for (int intento = 0; intento < 3; intento++) {
            assertThat(limitador.tryAcquire("10.0.0.1", AHORA)).isTrue();
        }
        assertThat(limitador.tryAcquire("10.0.0.1", AHORA)).isFalse();
    }

    @Test
    void una_ip_no_consume_el_cupo_de_otra() {
        TokenBucketRateLimiter limitador = new TokenBucketRateLimiter(1, Duration.ofMinutes(1));

        assertThat(limitador.tryAcquire("10.0.0.1", AHORA)).isTrue();
        assertThat(limitador.tryAcquire("10.0.0.1", AHORA)).isFalse();
        assertThat(limitador.tryAcquire("10.0.0.2", AHORA)).isTrue();
    }

    @Test
    void se_rellena_con_el_tiempo_en_vez_de_por_ventanas_fijas() {
        TokenBucketRateLimiter limitador = new TokenBucketRateLimiter(60, Duration.ofMinutes(1));

        for (int intento = 0; intento < 60; intento++) {
            limitador.tryAcquire("10.0.0.1", AHORA);
        }

        assertThat(limitador.tryAcquire("10.0.0.1", AHORA)).isFalse();
        assertThat(limitador.tryAcquire("10.0.0.1", AHORA.plusSeconds(1))).isTrue();
    }

    @Test
    void los_cubos_inactivos_se_tiran_para_que_el_limitador_no_tenga_su_propia_fuga() {
        TokenBucketRateLimiter limitador = new TokenBucketRateLimiter(5, Duration.ofMinutes(1));
        limitador.tryAcquire("10.0.0.1", AHORA);

        limitador.evictIdle(AHORA.plus(Duration.ofMinutes(10)));
        assertThat(limitador.trackedKeys()).isEqualTo(1);

        limitador.evictIdle(AHORA.plus(Duration.ofHours(1)));
        assertThat(limitador.trackedKeys()).isZero();
    }
}
