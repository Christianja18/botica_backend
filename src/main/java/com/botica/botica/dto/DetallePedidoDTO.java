package com.botica.botica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedidoDTO {

    private Integer idDetalle;

    @NotNull(message = "El pedido es obligatorio")
    private Integer idPedido;

    private PedidoResumenDTO pedido;

    @NotNull(message = "El producto es obligatorio")
    private Integer idProducto;

    private ProductoResumenDTO producto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio unitario debe tener maximo 8 digitos enteros y 2 decimales")
    private BigDecimal precioUnitario;

    @DecimalMin(value = "0.00", message = "El subtotal debe ser mayor o igual a 0")
    @Digits(integer = 8, fraction = 2, message = "El subtotal debe tener maximo 8 digitos enteros y 2 decimales")
    private BigDecimal subtotal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PedidoResumenDTO {
        private Integer idPedido;
        private String fechaPedido;
        private BigDecimal total;
        private String estado;
        private ClienteResumenDTO cliente;
        private UsuarioResumenDTO usuario;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClienteResumenDTO {
        private Integer idCliente;
        private String nombre;
        private String apellido;
        private String dni;
        private String telefono;
        private String email;
    }

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductoResumenDTO {
        private Integer idProducto;
        private String nombre;
        private String codigoBarras;
        private String descripcion;
        private BigDecimal precioVenta;
        private Boolean requiereReceta;
        private String fechaVencimiento;
    }

    public interface OnCreate {}
    public interface OnUpdate {}
}
