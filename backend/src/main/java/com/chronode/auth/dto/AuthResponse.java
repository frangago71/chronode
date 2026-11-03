package com.chronode.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Respuesta de autenticación con token de acceso")
public record AuthResponse(
    @Schema(description = "Token de acceso JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,

    @Schema(description = "Tipo de esquema de autorización", example = "Bearer")
    String tokenType,

    @Schema(description = "Tiempo de validez del token en segundos", example = "3600")
    long expiresInSeconds,

    @Schema(description = "Identificador único del usuario", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID userId,

    @Schema(description = "Correo electrónico del usuario autenticado", example = "usuario@chronode.com")
    String email,

    @Schema(description = "Nombre completo del usuario", example = "Carlos Gómez")
    String fullName
) {}
