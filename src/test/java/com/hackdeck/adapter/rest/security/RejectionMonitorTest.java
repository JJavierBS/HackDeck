package com.hackdeck.adapter.rest.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RejectionMonitorTest {

    private final RejectionMonitor monitor = new RejectionMonitor();

    @Test
    void sin_rechazos_no_dice_nada() {
        assertThat(monitor.drainSummary()).isEmpty();
    }

    @Test
    void resume_cuantas_veces_y_cuantas_direcciones_distintas() {
        monitor.record("cupo_por_direccion", "10.0.0.1");
        monitor.record("cupo_por_direccion", "10.0.0.1");
        monitor.record("cupo_por_direccion", "10.0.0.2");

        assertThat(monitor.drainSummary()).hasValue("cupo_por_direccion=3 (2 direcciones)");
    }

    @Test
    void pone_delante_el_motivo_mas_frecuente() {
        monitor.record("cuerpo_demasiado_grande", "10.0.0.1");
        monitor.record("cupo_por_direccion", "10.0.0.2");
        monitor.record("cupo_por_direccion", "10.0.0.3");

        assertThat(monitor.drainSummary().orElseThrow())
                .startsWith("cupo_por_direccion=2")
                .contains("cuerpo_demasiado_grande=1");
    }

    @Test
    void al_resumir_se_vacia_para_que_el_siguiente_minuto_empiece_de_cero() {
        monitor.record("cupo_por_direccion", "10.0.0.1");
        monitor.drainSummary();

        assertThat(monitor.drainSummary()).isEmpty();
    }
}
