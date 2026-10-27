package com.chronode.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chronode.auth.dto.AuthResponse;
import com.chronode.auth.dto.LoginRequest;
import com.chronode.auth.dto.RegisterRequest;
import com.chronode.config.SecurityConfig;
import com.chronode.exception.GlobalExceptionHandler;
import com.chronode.security.CustomUserDetailsService;
import com.chronode.security.jwt.JwtAuthenticationEntryPoint;
import com.chronode.security.jwt.JwtAuthenticationFilter;
import com.chronode.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Debería retornar 200 OK y token cuando el login es válido")
    void deberiaRetornar200Ok_cuandoLoginEsValido() throws Exception {
        final LoginRequest request = new LoginRequest("usuario@chronode.com", "password123");
        final UUID userId = UUID.randomUUID();
        final AuthResponse mockResponse = new AuthResponse(
            "fake.jwt.token",
            "Bearer",
            3600,
            userId,
            "usuario@chronode.com",
            "Usuario Test"
        );
        given(authService.login(any(LoginRequest.class))).willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("fake.jwt.token"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.email").value("usuario@chronode.com"));
    }

    @Test
    @DisplayName("Debería retornar 400 Bad Request cuando el login tiene datos incompletos")
    void deberiaRetornar400BadRequest_cuandoLoginTieneCamposInvalidos() throws Exception {
        final LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Error de validación"));
    }

    @Test
    @DisplayName("Debería retornar 201 Created cuando el registro es exitoso")
    void deberiaRetornar201Created_cuandoRegistroEsValido() throws Exception {
        final RegisterRequest request = new RegisterRequest("nuevo@chronode.com", "password123", "Nuevo Usuario");
        final UUID userId = UUID.randomUUID();
        final AuthResponse mockResponse = new AuthResponse(
            "fake.jwt.token",
            "Bearer",
            3600,
            userId,
            "nuevo@chronode.com",
            "Nuevo Usuario"
        );
        given(authService.register(any(RegisterRequest.class))).willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").value("fake.jwt.token"))
            .andExpect(jsonPath("$.email").value("nuevo@chronode.com"));
    }

    @Test
    @DisplayName("Debería retornar 400 Bad Request cuando la contraseña de registro es demasiado corta")
    void deberiaRetornar400BadRequest_cuandoPasswordEsCorta() throws Exception {
        final RegisterRequest request = new RegisterRequest("nuevo@chronode.com", "123", "Nuevo Usuario");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Error de validación"));
    }

    @Test
    @DisplayName("Debería retornar 400 Bad Request cuando el email ya está registrado")
    void deberiaRetornar400BadRequest_cuandoEmailYaEstaRegistrado() throws Exception {
        final RegisterRequest request = new RegisterRequest("existente@chronode.com", "password123", "Usuario");
        given(authService.register(any(RegisterRequest.class)))
            .willThrow(new IllegalArgumentException("El correo electrónico ya se encuentra registrado"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Parámetro inválido"));
    }
}
