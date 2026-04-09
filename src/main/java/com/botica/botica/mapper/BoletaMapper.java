package com.botica.botica.mapper;

import com.botica.botica.dto.BoletaDTO;
import com.botica.botica.dto.BoletaPedidoDTO;
import com.botica.botica.entity.Boleta;
import com.botica.botica.entity.Cliente;
import com.botica.botica.entity.DetallePedido;
import com.botica.botica.entity.Pedido;
import com.botica.botica.entity.Producto;
import com.botica.botica.entity.Usuario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
public class BoletaMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public BoletaDTO toDTO(Boleta boleta) {
        if (boleta == null) {
            return null;
        }

        BoletaDTO dto = BoletaDTO.builder()
                .idBoleta(boleta.getIdBoleta())
                .numeroBoleta(boleta.getNumeroBoleta())
                .total(boleta.getTotal())
                .igv(boleta.getIgv())
                .totalConIgv(resolveTotalConIgv(boleta))
                .datosCliente(boleta.getDatosCliente())
                .datosEmpleado(boleta.getDatosEmpleado())
                .impresa(boleta.getImpresa())
                .build();

        if (boleta.getPedido() != null) {
            dto.setIdPedido(boleta.getPedido().getIdPedido());
            dto.setPedido(toPedidoDTO(boleta.getPedido()));
        }

        if (boleta.getFechaEmision() != null) {
            dto.setFechaEmision(boleta.getFechaEmision().format(DATE_TIME_FORMATTER));
        }

        return dto;
    }

    private BoletaPedidoDTO toPedidoDTO(Pedido pedido) {
        if (pedido == null) {
            return null;
        }

        return BoletaPedidoDTO.builder()
                .idPedido(pedido.getIdPedido())
                .fechaPedido(pedido.getFechaPedido() != null ? pedido.getFechaPedido().format(DATE_TIME_FORMATTER) : null)
                .total(pedido.getTotal())
                .estado(pedido.getEstado() != null ? pedido.getEstado().name() : null)
                .cliente(toClienteDTO(pedido.getCliente()))
                .usuario(toUsuarioDTO(pedido.getUsuario()))
                .detalles(toDetalleDTOs(pedido.getDetalles()))
                .build();
    }

    private BoletaPedidoDTO.ClienteResumenDTO toClienteDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        return BoletaPedidoDTO.ClienteResumenDTO.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .dni(cliente.getDni())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .build();
    }

    private BoletaPedidoDTO.UsuarioResumenDTO toUsuarioDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return BoletaPedidoDTO.UsuarioResumenDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .build();
    }

    private List<BoletaPedidoDTO.DetalleResumenDTO> toDetalleDTOs(List<DetallePedido> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return Collections.emptyList();
        }

        return detalles.stream()
                .map(this::toDetalleDTO)
                .toList();
    }

    private BoletaPedidoDTO.DetalleResumenDTO toDetalleDTO(DetallePedido detalle) {
        if (detalle == null) {
            return null;
        }

        return BoletaPedidoDTO.DetalleResumenDTO.builder()
                .idDetalle(detalle.getIdDetalle())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(resolveSubtotal(detalle))
                .producto(toProductoDTO(detalle.getProducto()))
                .build();
    }

    private BoletaPedidoDTO.ProductoResumenDTO toProductoDTO(Producto producto) {
        if (producto == null) {
            return null;
        }

        return BoletaPedidoDTO.ProductoResumenDTO.builder()
                .idProducto(producto.getIdProducto())
                .nombre(producto.getNombre())
                .codigoBarras(producto.getCodigoBarras())
                .descripcion(producto.getDescripcion())
                .precioVenta(producto.getPrecioVenta())
                .requiereReceta(producto.getRequiereReceta())
                .fechaVencimiento(producto.getFechaVencimiento() != null
                        ? producto.getFechaVencimiento().format(DATE_ONLY_FORMATTER)
                        : null)
                .build();
    }

    private BigDecimal resolveSubtotal(DetallePedido detalle) {
        if (detalle.getSubtotal() != null) {
            return detalle.getSubtotal();
        }
        if (detalle.getCantidad() == null || detalle.getPrecioUnitario() == null) {
            return null;
        }
        return detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()));
    }

    private BigDecimal resolveTotalConIgv(Boleta boleta) {
        if (boleta.getTotalConIgv() != null) {
            return boleta.getTotalConIgv();
        }
        if (boleta.getTotal() == null) {
            return null;
        }
        return boleta.getTotal().add(boleta.getIgv() == null ? BigDecimal.ZERO : boleta.getIgv());
    }
}
