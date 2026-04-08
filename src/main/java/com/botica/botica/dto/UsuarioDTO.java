package com.botica.botica.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para usuario")
public class UsuarioDTO {

    @Schema(description = "ID único del usuario", example = "1")
    private Integer idUsuario;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Nombre del usuario", example = "Juan", required = true)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    @Schema(description = "Apellido del usuario", example = "Pérez", required = true)
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @Schema(description = "Email único del usuario", example = "juan@botica.com", required = true)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria", groups = OnCreate.class)
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres", groups = OnCreate.class)
    @Schema(description = "Hash de la contraseña (mínimo 8 caracteres)", example = "SecurePass123", required = true)
    private String passwordHash;

    @NotNull(message = "El estado activo es obligatorio")
    @Builder.Default
    @Schema(description = "Indica si el usuario está activo", example = "true", defaultValue = "true")
    private Boolean activo = true;

    @NotNull(message = "El rol es obligatorio")
    @Schema(description = "ID del rol asignado al usuario", example = "1", required = true)
    private Integer idRol;

    @Schema(description = "Fecha de creación del usuario", example = "07/04/2026 22:40")
    private String fechaCreacion;

    // Grupos de validación
    public interface OnCreate {}
    public interface OnUpdate {}
}
