package com.botica.botica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
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

    private ClienteResumenDTO cliente;

    @NotNull(message = "El usuario es obligatorio")
    private Integer idUsuario;

    private UsuarioResumenDTO usuario;

    private String fechaPedido;

    @DecimalMin(value = "0.00", message = "El total debe ser mayor o igual a 0")
    @Digits(integer = 8, fraction = 2, message = "El total debe tener maximo 8 digitos enteros y 2 decimales")
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @NotNull(message = "El estado es obligatorio")
    @Builder.Default
    private String estado = "pendiente";

    private List<DetallePedidoDTO> detalles;

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

    public interface OnCreate {}
    public interface OnUpdate {}
}
