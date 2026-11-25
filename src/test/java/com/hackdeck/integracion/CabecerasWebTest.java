package com.hackdeck.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CabecerasWebTest {

    @Value("${local.server.port}")
    private int puerto;

    private final HttpClient cliente = HttpClient.newHttpClient();

    private HttpResponse<String> get(String ruta) throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta)).build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void una_ruta_que_no_existe_da_404_y_no_la_pagina() throws Exception {
        assertThat(get("/esta-pagina-no-existe").statusCode()).isEqualTo(404);
        assertThat(get("/api/v1/no-existe").statusCode()).isEqualTo(404);
    }

    @Test
    void la_respuesta_se_anuncia_en_espanol_y_sin_hsts_por_http() throws Exception {
        HttpResponse<String> respuesta = get("/esta-pagina-no-existe");
        assertThat(respuesta.headers().firstValue("Content-Language")).hasValue("es");
        assertThat(respuesta.headers().firstValue("Strict-Transport-Security")).isEmpty();
    }
}
