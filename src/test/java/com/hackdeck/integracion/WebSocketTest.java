package com.hackdeck.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.CloseStatus;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketTest {

    @Value("${local.server.port}")
    private int puerto;

    @Value("${hackdeck.limits.max-sockets-per-participant}")
    private int maxConexiones;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    private JsonNode crearPartida() throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + "/api/v1/games"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        return json.readTree(cliente.send(peticion, HttpResponse.BodyHandlers.ofString()).body());
    }

    private WebSocket conectar(String partida, String token, CompletableFuture<String> primerMensaje) {
        return conectar(partida, token, primerMensaje, new CompletableFuture<>());
    }

    private WebSocket conectar(
            String partida,
            String token,
            CompletableFuture<String> primerMensaje,
            CompletableFuture<Integer> alCerrarse) {
        return cliente.newWebSocketBuilder()
                .buildAsync(
                        URI.create("ws://localhost:" + puerto + "/ws/games/" + partida + "?token=" + token),
                        new WebSocket.Listener() {
                            @Override
                            public CompletionStage<?> onText(WebSocket socket, CharSequence datos, boolean ultimo) {
                                primerMensaje.complete(datos.toString());
                                socket.request(1);
                                return null;
                            }

                            @Override
                            public CompletionStage<?> onClose(WebSocket socket, int codigo, String motivo) {
                                alCerrarse.complete(codigo);
                                return null;
                            }
                        })
                .join();
    }

    @Test
    void al_conectar_con_un_token_valido_se_recibe_el_estado_de_entrada() throws Exception {
        JsonNode instructor = crearPartida();
        CompletableFuture<String> mensaje = new CompletableFuture<>();

        WebSocket socket = conectar(
                instructor.get("gameId").asString(), instructor.get("token").asString(), mensaje);

        String recibido = mensaje.get(5, TimeUnit.SECONDS);
        assertThat(json.readTree(recibido).get("type").asString()).isEqualTo("state");
        assertThat(json.readTree(recibido).get("state").get("joinCode").asString())
                .isEqualTo(instructor.get("joinCode").asString());
        socket.sendClose(WebSocket.NORMAL_CLOSURE, "fin");
    }

    /**
     * Un token valido abria conexiones sin limite, y el difusor proyecta el
     * estado una vez por cada una: el coste era del servidor, no de quien las
     * abria.
     */
    @Test
    void pasado_el_tope_de_conexiones_se_cierra_la_mas_antigua() throws Exception {
        JsonNode instructor = crearPartida();
        String partida = instructor.get("gameId").asString();
        String token = instructor.get("token").asString();

        CompletableFuture<Integer> primeraCerrada = new CompletableFuture<>();
        List<WebSocket> abiertas = new ArrayList<>();
        abiertas.add(conectar(partida, token, new CompletableFuture<>(), primeraCerrada));
        for (int extra = 0; extra < maxConexiones; extra++) {
            abiertas.add(conectar(partida, token, new CompletableFuture<>()));
        }

        // La ultima en llegar es la que el usuario esta mirando, asi que la
        // que se va es la primera.
        assertThat(primeraCerrada.get(5, TimeUnit.SECONDS)).isEqualTo(CloseStatus.POLICY_VIOLATION.getCode());
        abiertas.forEach(socket -> socket.sendClose(WebSocket.NORMAL_CLOSURE, "fin"));
    }

    @Test
    void sin_token_o_con_uno_de_otra_partida_no_se_deja_conectar() throws Exception {
        JsonNode instructor = crearPartida();
        String partida = instructor.get("gameId").asString();

        assertThatThrownBy(() -> conectar(partida, "", new CompletableFuture<>()))
                .isInstanceOf(CompletionException.class);
        assertThatThrownBy(() -> conectar(
                "11111111-1111-1111-1111-111111111111",
                instructor.get("token").asString(),
                new CompletableFuture<>()))
                .isInstanceOf(CompletionException.class);
    }
}
