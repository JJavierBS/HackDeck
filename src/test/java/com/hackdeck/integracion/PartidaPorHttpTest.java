package com.hackdeck.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Levanta la aplicacion entera y juega una ronda por HTTP. Es lo unico que
 * prueba el cableado completo: seguridad, controlador, motor y catalogo.
 *
 * Usa el cliente del JDK a proposito, para no atarse a la API de test de
 * turno de Spring.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PartidaPorHttpTest {

    @Value("${local.server.port}")
    private int puerto;

    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();

    private HttpResponse<String> peticion(String metodo, String ruta, String cuerpo, String token) throws Exception {
        HttpRequest.Builder peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json");
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        peticion.method(metodo, cuerpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(cuerpo));
        return cliente.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode cuerpoDe(HttpResponse<String> respuesta) {
        return json.readTree(respuesta.body());
    }

    @Test
    void se_juega_una_ronda_entera_por_http() throws Exception {
        HttpResponse<String> creada = peticion("POST", "/api/v1/games", "{\"roundsPerHalf\":2}", null);
        assertThat(creada.statusCode()).isEqualTo(201);
        JsonNode instructor = cuerpoDe(creada);
        String partida = instructor.get("gameId").asString();
        String codigo = instructor.get("joinCode").asString();
        String tokenInstructor = instructor.get("token").asString();

        String tokenRojos = cuerpoDe(peticion("POST", "/api/v1/games/join",
                "{\"code\":\"" + codigo + "\",\"displayName\":\"Rojos\"}", null)).get("token").asString();
        String tokenAzules = cuerpoDe(peticion("POST", "/api/v1/games/join",
                "{\"code\":\"" + codigo + "\",\"displayName\":\"Azules\"}", null)).get("token").asString();

        assertThat(peticion("POST", "/api/v1/games/" + partida + "/start", null, tokenInstructor).statusCode())
                .isEqualTo(202);

        JsonNode catalogo = cuerpoDe(peticion("GET", "/api/v1/catalog", null, tokenRojos));
        assertThat(catalogo).isNotEmpty();
        String carta = cartaAsequible(catalogo);

        assertThat(peticion("POST", "/api/v1/games/" + partida + "/actions",
                "{\"cardId\":\"" + carta + "\",\"parameters\":{}}", tokenRojos).statusCode()).isEqualTo(202);

        JsonNode tras = cuerpoDe(peticion("POST", "/api/v1/games/" + partida + "/rounds/resolve", null, tokenInstructor));
        assertThat(tras.get("currentRoundNumber").asInt()).isEqualTo(2);

        JsonNode vistaAzules = cuerpoDe(peticion("GET", "/api/v1/games/" + partida, null, tokenAzules));
        assertThat(vistaAzules.get("yourSide").asString()).isEqualTo("DEFENDER");

        JsonNode historial = cuerpoDe(peticion("GET", "/api/v1/games/" + partida + "/history", null, tokenInstructor));
        assertThat(historial.get("events")).isNotEmpty();
    }

    private static String cartaAsequible(JsonNode catalogo) {
        for (JsonNode carta : catalogo) {
            if ("ACTION".equals(carta.get("type").asString()) && carta.get("cost").asInt() <= 20) {
                return carta.get("id").asString();
            }
        }
        throw new IllegalStateException("el catalogo no trae ninguna carta que quepa en el presupuesto inicial");
    }

    @Test
    void sin_token_no_se_entra_y_un_equipo_no_puede_arbitrar() throws Exception {
        JsonNode instructor = cuerpoDe(peticion("POST", "/api/v1/games", "{}", null));
        String partida = instructor.get("gameId").asString();
        String tokenRojos = cuerpoDe(peticion("POST", "/api/v1/games/join",
                "{\"code\":\"" + instructor.get("joinCode").asString() + "\",\"displayName\":\"Rojos\"}", null))
                .get("token").asString();

        assertThat(peticion("GET", "/api/v1/games/" + partida, null, null).statusCode()).isEqualTo(401);
        assertThat(peticion("GET", "/api/v1/games/" + partida, null, "esto-no-es-un-token").statusCode())
                .isEqualTo(401);
        assertThat(peticion("POST", "/api/v1/games/" + partida + "/rounds/resolve", null, tokenRojos).statusCode())
                .isEqualTo(403);
        assertThat(peticion("GET", "/api/v1/games/" + partida + "/history", null, tokenRojos).statusCode())
                .isEqualTo(403);
    }

    @Test
    void una_carta_que_no_existe_se_rechaza_con_un_error_legible() throws Exception {
        JsonNode instructor = cuerpoDe(peticion("POST", "/api/v1/games", "{}", null));
        String partida = instructor.get("gameId").asString();
        String codigo = instructor.get("joinCode").asString();
        String tokenRojos = cuerpoDe(peticion("POST", "/api/v1/games/join",
                "{\"code\":\"" + codigo + "\",\"displayName\":\"Rojos\"}", null)).get("token").asString();
        cuerpoDe(peticion("POST", "/api/v1/games/join",
                "{\"code\":\"" + codigo + "\",\"displayName\":\"Azules\"}", null));
        peticion("POST", "/api/v1/games/" + partida + "/start", null, instructor.get("token").asString());

        HttpResponse<String> respuesta = peticion("POST", "/api/v1/games/" + partida + "/actions",
                "{\"cardId\":\"carta-inventada\",\"parameters\":{}}", tokenRojos);

        assertThat(respuesta.statusCode()).isEqualTo(400);
        assertThat(cuerpoDe(respuesta).get("error").asString()).isEqualTo("carta_desconocida");
    }
}
