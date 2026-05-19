package com.botica.botica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para usuario")
public class UsuarioDTO {

    @Schema(description = "ID unico del usuario", example = "1")
    private Integer idUsuario;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Nombre del usuario", example = "Juan", required = true)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    @Schema(description = "Apellido del usuario", example = "Perez", required = true)
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato valido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @Schema(description = "Email unico del usuario", example = "juan@botica.com", required = true)
    private String email;

    @NotBlank(message = "La contrasena es obligatoria", groups = OnCreate.class)
    @Size(min = 8, message = "La contrasena debe tener minimo 8 caracteres", groups = OnCreate.class)
    @Schema(description = "Contrasena en texto plano solo para crear o rotar credencial. Nunca se retorna.", example = "SecurePass123", required = true, accessMode = Schema.AccessMode.WRITE_ONLY)
    private String passwordHash;

    @NotNull(message = "El estado activo es obligatorio")
    @Builder.Default
    @Schema(description = "Indica si el usuario esta activo", example = "true", defaultValue = "true")
    private Boolean activo = true;

    @NotNull(message = "El rol es obligatorio")
    @Schema(description = "ID del rol asignado al usuario", example = "1", required = true)
    private Integer idRol;

    private RolResumenDTO rol;

    @Schema(description = "Fecha de creacion del usuario", example = "07/04/2026 22:40")
    private String fechaCreacion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RolResumenDTO {
        private Integer idRol;
        private String nombre;
        private String descripcion;
        private Boolean activo;
        private Boolean puedeVender;
        private Boolean puedeAdministrarInventario;
        private Boolean puedeVerReportes;
        private Boolean puedeAdministrarUsuarios;
    }

    public interface OnCreate {}
    public interface OnUpdate {}
}
