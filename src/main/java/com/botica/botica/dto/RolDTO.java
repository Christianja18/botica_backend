package com.botica.botica.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolDTO {

    private Integer idRol;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 50, message = "El nombre del rol no puede exceder 50 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    @NotNull(message = "El permiso para vender es obligatorio")
    @Builder.Default
    private Boolean puedeVender = true;

    @NotNull(message = "El permiso para administrar inventario es obligatorio")
    @Builder.Default
    private Boolean puedeAdministrarInventario = false;

    @NotNull(message = "El permiso para ver reportes es obligatorio")
    @Builder.Default
    private Boolean puedeVerReportes = false;

    @NotNull(message = "El permiso para administrar usuarios es obligatorio")
    @Builder.Default
    private Boolean puedeAdministrarUsuarios = false;

    @NotNull(message = "El estado activo es obligatorio")
    @Builder.Default
    private Boolean activo = true;

    private String fechaCreacion;

    // Grupos de validación
    public interface OnCreate {}
    public interface OnUpdate {}
}