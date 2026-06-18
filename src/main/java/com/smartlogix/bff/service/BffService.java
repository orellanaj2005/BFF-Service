package com.smartlogix.bff.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Patrón de diseño: Facade
 *
 * BffService actúa como fachada de dos microservicios (User-Service e
 * Inventory-Service). El controller no conoce la existencia de estos
 * servicios ni cómo se llaman — solo interactúa con este servicio.
 *
 * Las llamadas se ejecutan en paralelo (CompletableFuture) para minimizar
 * la latencia total: el tiempo de respuesta es max(t_usuario, t_inventario)
 * en lugar de t_usuario + t_inventario.
 */
@Service
public class BffService {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient usuarioClient;
    private final RestClient inventarioClient;

    public BffService(
            @Qualifier("usuarioClient")   RestClient usuarioClient,
            @Qualifier("inventarioClient") RestClient inventarioClient) {
        this.usuarioClient   = usuarioClient;
        this.inventarioClient = inventarioClient;
    }

    /**
     * Agrega en una sola llamada los datos del dashboard de usuarios
     * y del dashboard de inventario, ejecutando ambas peticiones en paralelo.
     */
    public Map<String, Object> getDashboard(String authHeader) {
        CompletableFuture<Map<String, Object>> usuariosFuture = CompletableFuture.supplyAsync(() ->
                usuarioClient.get()
                        .uri("/usuarios/dashboard")
                        .header("Authorization", authHeader)
                        .retrieve()
                        .body(MAP_TYPE)
        );

        CompletableFuture<Map<String, Object>> inventarioFuture = CompletableFuture.supplyAsync(() ->
                inventarioClient.get()
                        .uri("/inventario/dashboard/resumen")
                        .header("Authorization", authHeader)
                        .retrieve()
                        .body(MAP_TYPE)
        );

        try {
            Map<String, Object> result = new HashMap<>();
            result.putAll(usuariosFuture.get());
            result.putAll(inventarioFuture.get());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error al agregar datos del dashboard: " + e.getMessage(), e);
        }
    }
}
