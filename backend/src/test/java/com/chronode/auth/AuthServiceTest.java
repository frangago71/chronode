package com.chronode.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.chronode.auth.dto.AuthResponse;
import com.chronode.auth.dto.LoginRequest;
import com.chronode.auth.dto.RegisterRequest;
import com.chronode.security.UserPrincipal;
import com.chronode.security.jwt.JwtTokenProvider;
import com.chronode.user.User;
import com.chronode.user.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Debería autenticar y retornar token cuando las credenciales son válidas")
    void deberiaRetornarAuthResponse_cuandoLoginEsExitoso() {
        final LoginRequest request = new LoginRequest("user@chronode.com", "password123");
        final UUID userId = UUID.randomUUID();
        final User user = User.builder()
            .email("user@chronode.com")
            .fullName("Carlos Gómez")
            .passwordHash("encodedPassword")
            .enabled(true)
            .build();
        user.setId(userId);
        final UserPrincipal principal = UserPrincipal.fromUser(user);

        final Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);
        given(jwtTokenProvider.generateToken(principal)).willReturn("mocked.jwt.token");
        given(jwtTokenProvider.getExpirationMs()).willReturn(3600000L);

        final AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@chronode.com");
        assertThat(response.fullName()).isEqualTo("Carlos Gómez");
    }

    @Test
    @DisplayName("Debería lanzar BadCredentialsException cuando las credenciales son incorrectas")
    void deberiaLanzarExcepcion_cuandoCredencialesSonInvalidas() {
        final LoginRequest request = new LoginRequest("user@chronode.com", "wrongPassword");
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .willThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("Credenciales inválidas");
    }

    @Test
    @DisplayName("Debería registrar un nuevo usuario y retornar token cuando los datos son válidos")
    void deberiaRegistrarUsuarioYRetornarAuthResponse_cuandoDatosSonValidos() {
        final RegisterRequest request = new RegisterRequest("new@chronode.com", "password123", "Nuevo Usuario");
        final UUID userId = UUID.randomUUID();
        final User savedUser = User.builder()
            .email("new@chronode.com")
            .fullName("Nuevo Usuario")
            .passwordHash("encodedPassword")
            .enabled(true)
            .build();
        savedUser.setId(userId);

        given(userRepository.existsByEmail("new@chronode.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtTokenProvider.generateToken(any(UserPrincipal.class))).willReturn("mocked.jwt.token");
        given(jwtTokenProvider.getExpirationMs()).willReturn(3600000L);

        final AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("new@chronode.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException cuando el email ya está registrado")
    void deberiaLanzarExcepcion_cuandoEmailYaEstaRegistrado() {
        final RegisterRequest request = new RegisterRequest("existing@chronode.com", "password123", "Usuario Existente");
        given(userRepository.existsByEmail("existing@chronode.com")).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ya se encuentra registrado");
    }
}
