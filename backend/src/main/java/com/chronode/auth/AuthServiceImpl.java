package com.chronode.auth;

import com.chronode.auth.dto.AuthResponse;
import com.chronode.auth.dto.LoginRequest;
import com.chronode.auth.dto.RegisterRequest;
import com.chronode.security.UserPrincipal;
import com.chronode.security.jwt.JwtTokenProvider;
import com.chronode.user.User;
import com.chronode.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(final LoginRequest request) {
        final Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        final UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        final String token = jwtTokenProvider.generateToken(principal);
        final long expiresInSeconds = jwtTokenProvider.getExpirationMs() / 1000;

        return new AuthResponse(
            token,
            "Bearer",
            expiresInSeconds,
            principal.getId(),
            principal.getEmail(),
            principal.getFullName()
        );
    }

    @Override
    @Transactional
    public AuthResponse register(final RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado: " + request.email());
        }

        final User user = User.builder()
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .fullName(request.fullName())
            .enabled(true)
            .build();

        final User savedUser = userRepository.save(user);
        final UserPrincipal principal = UserPrincipal.fromUser(savedUser);
        final String token = jwtTokenProvider.generateToken(principal);
        final long expiresInSeconds = jwtTokenProvider.getExpirationMs() / 1000;

        return new AuthResponse(
            token,
            "Bearer",
            expiresInSeconds,
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFullName()
        );
    }
}
