package com.chronode.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para el registro de un nuevo usuario")
public record RegisterRequest(
    @Schema(description = "Correo electrónico del usuario", example = "usuario@chronode.com")
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    String email,

    @Schema(description = "Contraseña segura (entre 8 y 100 caracteres)", example = "Password123!")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    String password,

    @Schema(description = "Nombre completo del usuario", example = "Carlos Gómez")
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100, message = "El nombre completo no puede superar los 100 caracteres")
    String fullName
) {}
