package com.chronode.config;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chronode.security.CustomUserDetailsService;
import com.chronode.security.UserPrincipal;
import com.chronode.security.jwt.JwtAuthenticationEntryPoint;
import com.chronode.security.jwt.JwtAuthenticationFilter;
import com.chronode.security.jwt.JwtTokenProvider;
import com.chronode.user.User;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(SecurityConfigTest.DummyProtectedController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, SecurityConfigTest.DummyProtectedController.class})
class SecurityConfigTest {

    @RestController
    @RequestMapping("/api/v1/secure")
    static class DummyProtectedController {
        @GetMapping("/data")
        public String getProtectedData() {
            return "protected-content";
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Debería rechazar con 401 Unauthorized en formato ProblemDetail cuando se accede a un endpoint protegido sin token")
    void deberiaRetornar401_cuandoSeAccedeAEndpointProtegidoSinToken() throws Exception {
        mockMvc.perform(get("/api/v1/secure/data"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.title").value("No autenticado"))
            .andExpect(jsonPath("$.type").value("https://chronode.com/errors/unauthorized"));
    }

    @Test
    @DisplayName("Debería permitir acceso con 200 OK cuando se proporciona un Bearer token válido")
    void deberiaPermitirAcceso_cuandoSeProporcionaTokenValido() throws Exception {
        final String token = "valid.jwt.token";
        final String email = "user@chronode.com";
        final User user = User.builder()
            .email(email)
            .fullName("Usuario Test")
            .passwordHash("hash")
            .enabled(true)
            .build();
        user.setId(UUID.randomUUID());
        final UserPrincipal principal = UserPrincipal.fromUser(user);

        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getEmailFromToken(token)).willReturn(email);
        given(customUserDetailsService.loadUserByUsername(email)).willReturn(principal);

        mockMvc.perform(get("/api/v1/secure/data")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
}
