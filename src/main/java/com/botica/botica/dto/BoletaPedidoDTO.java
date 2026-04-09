package com.botica.botica.dto;

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
public class BoletaPedidoDTO {

    private Integer idPedido;
    private String fechaPedido;
    private BigDecimal total;
    private String estado;
    private ClienteResumenDTO cliente;
    private UsuarioResumenDTO usuario;
    private List<DetalleResumenDTO> detalles;

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
    public static class DetalleResumenDTO {
        private Integer idDetalle;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
        private ProductoResumenDTO producto;
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
}
