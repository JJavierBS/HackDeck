package com.hackdeck.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cupos con valores de juguete para poder rozarlos en un test. Van en su
 * propio contexto: los cubos del limitador son del proceso, y agotarlos en
 * el contexto compartido dejaria a los demas tests sin poder unirse.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "hackdeck.limits.join-attempts-per-ip=3",
                "hackdeck.limits.max-live-games=1"
        })
class LimitesDeAbusoTest {

    @Value("${local.server.port}")
    private int puerto;

    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();

    private HttpResponse<String> post(String ruta, String cuerpo) throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json")
                .POST(cuerpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void agotado_el_cupo_de_uniones_la_ip_recibe_429() throws Exception {
        String union = "{\"code\":\"ZZZZZZ\",\"displayName\":\"Curiosos\"}";

        for (int intento = 0; intento < 3; intento++) {
            assertThat(post("/api/v1/games/join", union).statusCode()).isEqualTo(404);
        }

        HttpResponse<String> cortada = post("/api/v1/games/join", union);
        assertThat(cortada.statusCode()).isEqualTo(429);
        assertThat(cortada.headers().firstValue("Retry-After")).isPresent();
        assertThat(json.readTree(cortada.body()).get("error").asString()).isEqualTo("demasiadas_peticiones");
    }

    @Test
    void con_el_servidor_lleno_crear_partida_devuelve_503() throws Exception {
        assertThat(post("/api/v1/games", "{}").statusCode()).isEqualTo(201);

        HttpResponse<String> llena = post("/api/v1/games", "{}");
        assertThat(llena.statusCode()).isEqualTo(503);
        assertThat(json.readTree(llena.body()).get("error").asString()).isEqualTo("servidor_lleno");
    }
}
