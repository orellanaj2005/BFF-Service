package com.smartlogix.bff.controller;

import com.smartlogix.bff.service.BffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/bff")
@Tag(name = "BFF", description = "Backend for Frontend — endpoints de agregación optimizados para el dashboard de SmartLogix.")
public class BffController {

    private final BffService bffService;

    public BffController(BffService bffService) {
        this.bffService = bffService;
    }

    @Operation(
        summary = "Dashboard agregado",
        description = "Agrega en una sola respuesta los datos de usuarios (User-Service) " +
                      "y de inventario (Inventory-Service) que necesita el dashboard. " +
                      "Las dos llamadas internas se ejecutan en paralelo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Datos del dashboard combinados"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "502", description = "Uno o más microservicios no respondieron")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(
            @Parameter(description = "JWT Bearer token", required = true)
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(bffService.getDashboard(authHeader));
    }
}
