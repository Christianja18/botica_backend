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
public class ReporteDTO {

    private Integer idReporte;

    @NotBlank(message = "El tipo de reporte es obligatorio")
    private String tipoReporte;

    private String fechaGeneracion;

    private String fechaInicio;

    private String fechaFin;

    @NotNull(message = "El usuario que genera el reporte es obligatorio")
    private Integer generadoPor;

    private String datos;  // JSON string

    @Size(max = 500, message = "La ruta del archivo no puede exceder 500 caracteres")
    private String archivoPath;

    // Grupos de validación
    public interface OnCreate {}
    public interface OnUpdate {}
}