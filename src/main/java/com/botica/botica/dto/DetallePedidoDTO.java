package com.botica.botica.dto;

import jakarta.validation.constraints.*;
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

    @NotNull(message = "El producto es obligatorio")
    private Integer idProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio unitario debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal precioUnitario;

    @DecimalMin(value = "0.00", message = "El subtotal debe ser mayor o igual a 0")
    @Digits(integer = 8, fraction = 2, message = "El subtotal debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal subtotal;

    // Grupos de validación
    public interface OnCreate {}
    public interface OnUpdate {}
}