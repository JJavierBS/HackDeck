package com.hackdeck.adapter.security;

import com.hackdeck.application.exception.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Quien puede crear partidas. Lo importante aqui es que sin clave configurada
 * queda abierto: en un despliegue publico lo que frena los abusos son los
 * cupos, no una clave compartida.
 */
class InstructorAccessGuardTest {

    private static final String DE_FUERA = "203.0.113.5";

    private InstructorAccessGuard guardCon(String clave) {
        return new InstructorAccessGuard(new SecurityProperties(
                "", Duration.ofHours(8), clave, false, List.of()));
    }

    private MockHttpServletRequest peticionDesde(String direccion, String claveEnviada) {
        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.setRemoteAddr(direccion);
        if (claveEnviada != null) {
            peticion.addHeader(InstructorAccessGuard.INSTRUCTOR_KEY_HEADER, claveEnviada);
        }
        return peticion;
    }

    @Test
    void sin_clave_configurada_cualquiera_puede_crear() {
        assertThatCode(() -> guardCon("").requireInstructorAccess(peticionDesde(DE_FUERA, null)))
                .doesNotThrowAnyException();
    }

    @Test
    void con_clave_configurada_se_vuelve_al_modo_cerrado() {
        InstructorAccessGuard guard = guardCon("la-clave");

        assertThatThrownBy(() -> guard.requireInstructorAccess(peticionDesde(DE_FUERA, null)))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> guard.requireInstructorAccess(peticionDesde(DE_FUERA, "otra")))
                .isInstanceOf(AccessDeniedException.class);
        assertThatCode(() -> guard.requireInstructorAccess(peticionDesde(DE_FUERA, "la-clave")))
                .doesNotThrowAnyException();
    }

    @Test
    void desde_el_propio_equipo_no_se_pide_nada_aunque_haya_clave() {
        InstructorAccessGuard guard = new InstructorAccessGuard(new SecurityProperties(
                "", Duration.ofHours(8), "la-clave", true, List.of()));

        assertThatCode(() -> guard.requireInstructorAccess(peticionDesde("127.0.0.1", null)))
                .doesNotThrowAnyException();
    }
}
