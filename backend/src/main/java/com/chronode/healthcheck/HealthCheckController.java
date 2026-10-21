package com.chronode.healthcheck;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "Endpoints de verificación de estado y conectividad del servicio")
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    @GetMapping
    @Operation(summary = "Verificar estado del servicio", description = "Retorna el estado operativo, versión y marca de tiempo del backend")
    public ResponseEntity<HealthCheckResponse> getHealth() {
        final HealthCheckResponse response = healthCheckService.checkHealth();
        return ResponseEntity.ok(response);
    }
}
