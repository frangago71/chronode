package com.chronode.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.chronode.security.UserPrincipal;
import com.chronode.user.User;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_MS = 3600000; // 1 hora

    private JwtTokenProvider jwtTokenProvider;
    private UserPrincipal testUserPrincipal;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION_MS);
        testUserId = UUID.randomUUID();

        final User user = User.builder()
            .email("test@chronode.com")
            .fullName("Usuario de Prueba")
            .passwordHash("hashedPassword")
            .enabled(true)
            .build();
        user.setId(testUserId);

        testUserPrincipal = UserPrincipal.fromUser(user);
    }

    @Test
    @DisplayName("Debería generar un token válido cuando se proporcionan datos de usuario")
    void deberiaGenerarTokenValido_cuandoSeProporcionanDatosDeUsuario() {
        final String token = jwtTokenProvider.generateToken(testUserPrincipal);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Debería extraer el userId y email correctos cuando el token es válido")
    void deberiaExtraerClaimsCorrectos_cuandoElTokenEsValido() {
        final String token = jwtTokenProvider.generateToken(testUserPrincipal);

        final UUID extractedUserId = jwtTokenProvider.getUserIdFromToken(token);
        final String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        assertThat(extractedUserId).isEqualTo(testUserId);
        assertThat(extractedEmail).isEqualTo("test@chronode.com");
    }

    @Test
    @DisplayName("Debería retornar false cuando se valida un token con firma alterada")
    void deberiaRetornarFalse_cuandoFirmaEsInvalida() {
        final JwtTokenProvider otherProvider = new JwtTokenProvider(
            "999E635266556A586E3272357538782F413F4428472B4B6250645367566B5999",
            EXPIRATION_MS
        );
        final String tokenFromOtherProvider = otherProvider.generateToken(testUserPrincipal);

        final boolean isValid = jwtTokenProvider.validateToken(tokenFromOtherProvider);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Debería retornar false cuando el token ha expirado")
    void deberiaRetornarFalse_cuandoTokenHaExpirado() {
        // Provider con expiración negativa (-1 segundo)
        final JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1000);
        final String expiredToken = expiredProvider.generateToken(testUserPrincipal);

        final boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Debería retornar false cuando el token es una cadena malformada")
    void deberiaRetornarFalse_cuandoTokenEsMalformado() {
        final boolean isValid = jwtTokenProvider.validateToken("token.invalido.malformado");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Debería retornar false cuando el token es nulo o vacío")
    void deberiaRetornarFalse_cuandoTokenEsNuloOVacio() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
        assertThat(jwtTokenProvider.validateToken("   ")).isFalse();
    }
}
