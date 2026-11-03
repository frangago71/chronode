package com.chronode.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.chronode.security.CustomUserDetailsService;
import com.chronode.security.UserPrincipal;
import com.chronode.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debería establecer autenticación en el SecurityContext cuando el token es válido")
    void deberiaEstablecerAutenticacion_cuandoTokenEsValido() throws ServletException, IOException {
        final String token = "valid.jwt.token";
        final String email = "usuario@chronode.com";
        final User user = User.builder()
            .email(email)
            .fullName("Juan Pérez")
            .passwordHash("hashed")
            .enabled(true)
            .build();
        user.setId(UUID.randomUUID());
        final UserPrincipal principal = UserPrincipal.fromUser(user);

        given(request.getHeader("Authorization")).willReturn("Bearer " + token);
        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getEmailFromToken(token)).willReturn(email);
        given(userDetailsService.loadUserByUsername(email)).willReturn(principal);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(email);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(principal);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Debería no establecer autenticación cuando el token es inválido")
    void deberiaNoEstablecerAutenticacion_cuandoTokenEsInvalido() throws ServletException, IOException {
        final String token = "invalid.jwt.token";

        given(request.getHeader("Authorization")).willReturn("Bearer " + token);
        given(jwtTokenProvider.validateToken(token)).willReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Debería no establecer autenticación cuando no hay encabezado Authorization")
    void deberiaNoEstablecerAutenticacion_cuandoNoHayEncabezadoAuthorization() throws ServletException, IOException {
        given(request.getHeader("Authorization")).willReturn(null);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Debería no establecer autenticación cuando el encabezado no tiene prefijo Bearer")
    void deberiaNoEstablecerAutenticacion_cuandoEncabezadoNoEsBearer() throws ServletException, IOException {
        given(request.getHeader("Authorization")).willReturn("Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
