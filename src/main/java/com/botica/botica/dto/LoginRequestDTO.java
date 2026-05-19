package com.botica.botica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato valido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @Schema(description = "Email del usuario", example = "admin@botica.com")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 128, message = "La contrasena debe tener entre 8 y 128 caracteres")
    @Schema(description = "Contrasena del usuario", accessMode = Schema.AccessMode.WRITE_ONLY, example = "Botica2026!")
    private String password;
}
