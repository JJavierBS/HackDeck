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
 * Comprueba que el cupo por creador llega cableado hasta el controlador, que
 * es quien decide que el creador es la direccion de la peticion.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "hackdeck.limits.max-live-games-per-ip=1",
                "hackdeck.limits.max-live-tournaments-per-ip=1"
        })
class CuotaPorIpTest {

    @Value("${local.server.port}")
    private int puerto;

    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();

    private HttpResponse<String> crear(String ruta) throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void la_segunda_partida_de_la_misma_ip_se_rechaza_con_su_propio_codigo() throws Exception {
        assertThat(crear("/api/v1/games").statusCode()).isEqualTo(201);

        HttpResponse<String> segunda = crear("/api/v1/games");

        assertThat(segunda.statusCode()).isEqualTo(429);
        // Distinto de servidor_lleno: el servidor tiene sitio, quien se ha
        // pasado es este cliente.
        assertThat(json.readTree(segunda.body()).get("error").asString()).isEqualTo("cuota_agotada");
    }

    @Test
    void el_cupo_de_torneos_va_por_su_cuenta() throws Exception {
        assertThat(crear("/api/v1/tournaments").statusCode()).isEqualTo(201);

        assertThat(crear("/api/v1/tournaments").statusCode()).isEqualTo(429);
    }
}
