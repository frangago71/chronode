package com.chronode.healthcheck;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HealthCheckServiceTest {

    private HealthCheckService healthCheckService;

    @BeforeEach
    void setUp() {
        healthCheckService = new HealthCheckServiceImpl();
    }

    @Test
    @DisplayName("Debería retornar estado UP cuando se verifica la salud del backend")
    void deberiaRetornarEstadoUp_cuandoSeVerificaSalud() {
        final HealthCheckResponse response = healthCheckService.checkHealth();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("UP");
        assertThat(response.application()).isEqualTo("Chronode Backend");
        assertThat(response.version()).isEqualTo("1.0.0");
        assertThat(response.timestamp()).isNotNull();
    }
}
