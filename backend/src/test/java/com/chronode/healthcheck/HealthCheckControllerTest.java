package com.chronode.healthcheck;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chronode.config.SecurityConfig;
import com.chronode.security.CustomUserDetailsService;
import com.chronode.security.jwt.JwtAuthenticationEntryPoint;
import com.chronode.security.jwt.JwtAuthenticationFilter;
import com.chronode.security.jwt.JwtTokenProvider;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthCheckController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthCheckService healthCheckService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Debería retornar 200 OK con los datos de salud cuando se consulta el endpoint")
    void deberiaRetornar200Ok_cuandoSeConsultaHealth() throws Exception {
        final HealthCheckResponse mockResponse = new HealthCheckResponse(
            "UP",
            "Chronode Backend",
            "1.0.0",
            "test",
            Instant.now()
        );
        given(healthCheckService.checkHealth()).willReturn(mockResponse);

        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.application").value("Chronode Backend"))
            .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
