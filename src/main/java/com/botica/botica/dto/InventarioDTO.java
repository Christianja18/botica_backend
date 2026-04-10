package com.botica.botica.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioDTO {

    private Integer idInventario;

    @NotNull(message = "El producto es obligatorio")
    private Integer idProducto;

    private ProductoResumenDTO producto;

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Integer stockActual;

    @NotNull(message = "El stock minimo es obligatorio")
    @Min(value = 0, message = "El stock minimo no puede ser negativo")
    private Integer stockMinimo;

    private String fechaActualizacion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductoResumenDTO {
        private Integer idProducto;
        private String nombre;
        private String codigoBarras;
        private String descripcion;
        private java.math.BigDecimal precioVenta;
        private Boolean requiereReceta;
        private String fechaVencimiento;
    }
}
