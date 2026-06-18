package com.smartlogix.bff.controller;

import com.smartlogix.bff.service.BffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BffController.class)
class BffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BffService bffService;

    @Test
    void dashboard_retornaDatosAgregados() throws Exception {
        Map<String, Object> datos = Map.of(
                "usuarios",        java.util.List.of(),
                "usuariosPorRol",  java.util.List.of(),
                "productos",       java.util.List.of(),
                "stockCritico",    java.util.List.of(),
                "ultimosRegistrados", java.util.List.of(),
                "totalProductos",  10L,
                "cantidadStockBajo", 2L,
                "valorInventario", 500000L
        );
        when(bffService.getDashboard("Bearer token-test")).thenReturn(datos);

        mockMvc.perform(get("/bff/dashboard")
                        .header("Authorization", "Bearer token-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductos").value(10))
                .andExpect(jsonPath("$.cantidadStockBajo").value(2))
                .andExpect(jsonPath("$.valorInventario").value(500000));
    }

    @Test
    void dashboard_sinAuthHeader_retorna400() throws Exception {
        mockMvc.perform(get("/bff/dashboard"))
                .andExpect(status().isBadRequest());
    }
}
