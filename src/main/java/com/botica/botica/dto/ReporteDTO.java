package com.botica.botica.dto;

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
public class ReporteDTO {

    private Integer idReporte;

    @NotBlank(message = "El tipo de reporte es obligatorio")
    private String tipoReporte;

    private String fechaGeneracion;

    private String fechaInicio;

    private String fechaFin;

    @NotNull(message = "El usuario que genera el reporte es obligatorio")
    private Integer generadoPor;

    private UsuarioResumenDTO usuarioGenerador;

    private String datos;

    @Size(max = 500, message = "La ruta del archivo no puede exceder 500 caracteres")
    private String archivoPath;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsuarioResumenDTO {
        private Integer idUsuario;
        private String nombre;
        private String apellido;
        private String email;
    }

    public interface OnCreate {}
    public interface OnUpdate {}
}
