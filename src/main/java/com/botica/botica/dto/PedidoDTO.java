package com.botica.botica.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDTO {

    private Integer idPedido;

    private Integer idCliente;

    @NotNull(message = "El usuario es obligatorio")
    private Integer idUsuario;

    private String fechaPedido;

    @DecimalMin(value = "0.00", message = "El total debe ser mayor o igual a 0")
    @Digits(integer = 8, fraction = 2, message = "El total debe tener máximo 8 dígitos enteros y 2 decimales")
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @NotNull(message = "El estado es obligatorio")
    @Builder.Default
    private String estado = "pendiente";

    private List<DetallePedidoDTO> detalles;

    // Grupos de validación
    public interface OnCreate {}
    public interface OnUpdate {}
}
