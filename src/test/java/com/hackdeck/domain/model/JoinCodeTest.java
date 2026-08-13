package com.hackdeck.domain.model;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JoinCodeTest {

    @RepeatedTest(20)
    void el_codigo_generado_no_lleva_caracteres_que_se_confundan_al_dictarlo() {
        String codigo = JoinCode.generate().value();

        assertThat(codigo).hasSize(6).doesNotContain("O", "0", "I", "1");
    }

    @Test
    void se_acepta_en_minusculas_y_con_espacios_porque_lo_teclea_un_alumno() {
        assertThat(JoinCode.of("  ab3d5f ").value()).isEqualTo("AB3D5F");
    }

    @Test
    void se_rechaza_lo_que_no_es_un_codigo() {
        assertThatThrownBy(() -> JoinCode.of("ABC")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JoinCode.of("ABCDE0")).isInstanceOf(IllegalArgumentException.class);
    }
}
