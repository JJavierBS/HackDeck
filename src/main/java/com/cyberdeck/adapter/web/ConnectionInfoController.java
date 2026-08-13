package com.cyberdeck.adapter.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Direcciones por las que los equipos pueden entrar. En el aula el
 * instructor arranca el jar en su portatil y los demas necesitan su IP
 * local, que no se sabe hasta que arranca.
 *
 * No pide token: solo dice en que direcciones responde esta maquina, que es
 * algo que cualquiera en la misma red puede averiguar.
 */
@RestController
@RequestMapping("/api/v1/connection")
public class ConnectionInfoController {

    private final int port;

    public ConnectionInfoController(@Value("${server.port:8080}") int port) {
        this.port = port;
    }

    @GetMapping
    public ConnectionInfo connectionInfo() {
        return new ConnectionInfo(localAddresses().stream().map(host -> "http://" + host + ":" + port).toList());
    }

    public record ConnectionInfo(List<String> urls) {
    }

    static List<String> localAddresses() {
        List<String> direcciones = new ArrayList<>();
        try {
            for (NetworkInterface red : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!red.isUp() || red.isLoopback()) {
                    continue;
                }
                for (InetAddress direccion : Collections.list(red.getInetAddresses())) {
                    if (direccion.isSiteLocalAddress()) {
                        direcciones.add(direccion.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            return List.of();
        }
        return direcciones;
    }
}
