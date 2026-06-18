package com.smartlogix.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${bff.services.usuario-url}")
    private String usuarioUrl;

    @Value("${bff.services.inventario-url}")
    private String inventarioUrl;

    @Bean("usuarioClient")
    public RestClient usuarioClient() {
        return RestClient.builder().baseUrl(usuarioUrl).build();
    }

    @Bean("inventarioClient")
    public RestClient inventarioClient() {
        return RestClient.builder().baseUrl(inventarioUrl).build();
    }
}
