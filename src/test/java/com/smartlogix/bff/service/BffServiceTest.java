package com.smartlogix.bff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BffServiceTest {

    private RestClient usuarioClient;
    private RestClient inventarioClient;
    private BffService bffService;

    @BeforeEach
    void setUp() {
        usuarioClient   = mock(RestClient.class);
        inventarioClient = mock(RestClient.class);
        bffService = new BffService(usuarioClient, inventarioClient);
    }

    @Test
    void getDashboard_combinaRespuestasDeAmbosServicios() {
        Map<String, Object> usuariosData   = Map.of("usuarios", java.util.List.of(), "usuariosPorRol", java.util.List.of());
        Map<String, Object> inventarioData = Map.of("totalProductos", 5L, "cantidadStockBajo", 1L, "valorInventario", 100L);

        mockRestClient(usuarioClient,   "/usuarios/dashboard",          usuariosData);
        mockRestClient(inventarioClient, "/inventario/dashboard/resumen", inventarioData);

        Map<String, Object> result = bffService.getDashboard("Bearer test");

        assertThat(result).containsKey("usuarios");
        assertThat(result).containsKey("totalProductos");
        assertThat(result.get("totalProductos")).isEqualTo(5L);
    }

    @Test
    void getDashboard_propagaExcepcionCuandoFallaUnServicio() {
        RestClient.RequestHeadersUriSpec<?> uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec<?> headersSpec = mock(RestClient.RequestHeadersSpec.class);

        doReturn(uriSpec).when(usuarioClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString());
        doReturn(headersSpec).when(headersSpec).header(anyString(), anyString());
        when(headersSpec.retrieve()).thenThrow(new RuntimeException("ms-usuario no disponible"));

        assertThatThrownBy(() -> bffService.getDashboard("Bearer test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al agregar datos del dashboard");
    }

    @SuppressWarnings("unchecked")
    private void mockRestClient(RestClient client, String uri, Map<String, Object> response) {
        RestClient.RequestHeadersUriSpec<?> uriSpec     = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec<?>    headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec             responseSpec = mock(RestClient.ResponseSpec.class);

        doReturn(uriSpec).when(client).get();
        doReturn(headersSpec).when(uriSpec).uri(uri);
        doReturn(headersSpec).when(headersSpec).header(anyString(), anyString());
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(response);
    }
}
