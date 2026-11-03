package com.chronode.security.jwt;

import com.chronode.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final String jwtSecret;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
        @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") final String jwtSecret,
        @Value("${jwt.expiration-ms:3600000}") final long jwtExpirationMs
    ) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(final Authentication authentication) {
        final UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(userPrincipal.getId(), userPrincipal.getEmail());
    }

    public String generateToken(final UserPrincipal userPrincipal) {
        return generateToken(userPrincipal.getId(), userPrincipal.getEmail());
    }

    public String generateToken(final UUID userId, final String email) {
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
            .subject(email)
            .claim("userId", userId.toString())
            .claim("email", email)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey(), Jwts.SIG.HS256)
            .compact();
    }

    public UUID getUserIdFromToken(final String token) {
        final Claims claims = parseClaims(token);
        final String userIdStr = claims.get("userId", String.class);
        if (userIdStr != null) {
            return UUID.fromString(userIdStr);
        }
        return UUID.fromString(claims.getSubject());
    }

    public String getEmailFromToken(final String token) {
        final Claims claims = parseClaims(token);
        final String email = claims.get("email", String.class);
        return email != null ? email : claims.getSubject();
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    public boolean validateToken(final String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Firma o formato de JWT no válido");
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado");
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT no soportado");
        } catch (IllegalArgumentException e) {
            log.warn("Claims de JWT vacíos o nulos");
        }
        return false;
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
            if (keyBytes.length < 32) {
                keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            final byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
