package com.botica.botica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    private Integer idProducto;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @NotBlank(message = "El codigo de barras es obligatorio")
    @Size(max = 50, message = "El codigo de barras no puede exceder 50 caracteres")
    private String codigoBarras;

    @Size(max = 500, message = "La descripcion no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio de venta debe tener maximo 8 digitos enteros y 2 decimales")
    private BigDecimal precioVenta;

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de compra debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio de compra debe tener maximo 8 digitos enteros y 2 decimales")
    private BigDecimal precioCompra;

    @NotNull(message = "La categoria es obligatoria")
    private Integer idCategoria;

    private CategoriaResumenDTO categoria;

    @NotNull(message = "El proveedor es obligatorio")
    private Integer idProveedor;

    private ProveedorResumenDTO proveedor;

    @NotNull(message = "El campo requiere receta es obligatorio")
    private Boolean requiereReceta = false;

    private String fechaVencimiento;

    private String fechaCreacion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoriaResumenDTO {
        private Integer idCategoria;
        private String nombre;
        private String descripcion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProveedorResumenDTO {
        private Integer idProveedor;
        private String nombre;
        private String ruc;
        private String telefono;
        private String email;
        private String direccion;
    }
}
