package com.chronode.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para iniciar sesión")
public record LoginRequest(
    @Schema(description = "Correo electrónico del usuario", example = "usuario@chronode.com")
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    String email,

    @Schema(description = "Contraseña de acceso del usuario", example = "Password123!")
    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}
