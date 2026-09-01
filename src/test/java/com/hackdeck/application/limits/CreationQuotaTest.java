package com.hackdeck.application.limits;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class CreationQuotaTest {

    private final Set<String> vivas = new HashSet<>();
    private final Predicate<String> sigueViva = vivas::contains;

    private void crea(CreationQuota<String> cupo, String creador, String id) {
        vivas.add(id);
        cupo.register(creador, id);
    }

    @Test
    void deja_crear_hasta_el_cupo_y_no_mas() {
        CreationQuota<String> cupo = new CreationQuota<>(2);

        assertThat(cupo.hasRoom("10.0.0.1", sigueViva)).isTrue();
        crea(cupo, "10.0.0.1", "partida-1");
        crea(cupo, "10.0.0.1", "partida-2");

        assertThat(cupo.hasRoom("10.0.0.1", sigueViva)).isFalse();
    }

    @Test
    void una_partida_caducada_libera_sitio() {
        CreationQuota<String> cupo = new CreationQuota<>(1);
        crea(cupo, "10.0.0.1", "partida-1");
        assertThat(cupo.hasRoom("10.0.0.1", sigueViva)).isFalse();

        vivas.remove("partida-1");

        assertThat(cupo.hasRoom("10.0.0.1", sigueViva)).isTrue();
    }

    @Test
    void el_cupo_de_una_direccion_no_gasta_el_de_otra() {
        CreationQuota<String> cupo = new CreationQuota<>(1);
        crea(cupo, "10.0.0.1", "partida-1");

        assertThat(cupo.hasRoom("10.0.0.1", sigueViva)).isFalse();
        assertThat(cupo.hasRoom("10.0.0.2", sigueViva)).isTrue();
    }

    @Test
    void al_morir_todas_deja_de_seguir_a_esa_direccion() {
        CreationQuota<String> cupo = new CreationQuota<>(2);
        crea(cupo, "10.0.0.1", "partida-1");
        assertThat(cupo.trackedCreators()).containsExactly("10.0.0.1");

        vivas.clear();
        cupo.liveCount("10.0.0.1", sigueViva);

        assertThat(cupo.trackedCreators()).isEmpty();
    }
}
