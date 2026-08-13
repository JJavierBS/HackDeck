package com.cyberdeck.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CiaStateTest {

    @Test
    void empieza_con_los_tres_pilares_intactos() {
        CiaState estado = CiaState.intact();

        for (CiaPillar pilar : CiaPillar.values()) {
            assertThat(estado.levelOf(pilar)).isEqualTo(100);
        }
    }

    @Test
    void un_pilar_nunca_baja_de_cero_por_mucho_dano_que_reciba() {
        CiaState estado = CiaState.intact().withImpact(CiaPillar.AVAILABILITY, -250);

        assertThat(estado.levelOf(CiaPillar.AVAILABILITY)).isZero();
        assertThat(estado.isPillarDown(CiaPillar.AVAILABILITY)).isTrue();
    }

    @Test
    void reparar_no_sube_de_cien() {
        CiaState estado = CiaState.intact().withImpact(CiaPillar.INTEGRITY, 40);

        assertThat(estado.levelOf(CiaPillar.INTEGRITY)).isEqualTo(100);
    }

    @Test
    void el_impacto_solo_afecta_al_pilar_indicado() {
        CiaState estado = CiaState.intact().withImpact(CiaPillar.CONFIDENTIALITY, -30);

        assertThat(estado.levelOf(CiaPillar.CONFIDENTIALITY)).isEqualTo(70);
        assertThat(estado.levelOf(CiaPillar.INTEGRITY)).isEqualTo(100);
        assertThat(estado.levelOf(CiaPillar.AVAILABILITY)).isEqualTo(100);
    }
}
