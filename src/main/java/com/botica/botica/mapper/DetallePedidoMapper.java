package com.botica.botica.mapper;

import com.botica.botica.dto.DetallePedidoDTO;
import com.botica.botica.entity.Cliente;
import com.botica.botica.entity.DetallePedido;
import com.botica.botica.entity.Pedido;
import com.botica.botica.entity.Producto;
import com.botica.botica.entity.Usuario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Component
public class DetallePedidoMapper {

    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public DetallePedidoDTO toDTO(DetallePedido detalle) {
        return toDTO(detalle, true);
    }

    public DetallePedidoDTO toDTOWithoutPedido(DetallePedido detalle) {
        return toDTO(detalle, false);
    }

    private DetallePedidoDTO toDTO(DetallePedido detalle, boolean includePedido) {
        if (detalle == null) {
            return null;
        }
        DetallePedidoDTO dto = DetallePedidoDTO.builder()
                .idDetalle(detalle.getIdDetalle())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(resolveSubtotal(detalle))
                .build();

        if (includePedido && detalle.getPedido() != null) {
            dto.setIdPedido(detalle.getPedido().getIdPedido());
            dto.setPedido(toPedidoDTO(detalle.getPedido()));
        } else if (detalle.getPedido() != null) {
            dto.setIdPedido(detalle.getPedido().getIdPedido());
        }

        if (detalle.getProducto() != null) {
            dto.setIdProducto(detalle.getProducto().getIdProducto());
            dto.setProducto(toProductoDTO(detalle.getProducto()));
        }

        return dto;
    }

    public DetallePedido toEntity(DetallePedidoDTO dto) {
        if (dto == null) {
            return null;
        }
        DetallePedido detalle = new DetallePedido();
        detalle.setIdDetalle(dto.getIdDetalle());
        detalle.setCantidad(dto.getCantidad());
        detalle.setPrecioUnitario(dto.getPrecioUnitario());
        return detalle;
    }

    public DetallePedido updateEntity(DetallePedidoDTO dto, DetallePedido detalle) {
        if (dto == null) {
            return detalle;
        }
        detalle.setCantidad(dto.getCantidad());
        detalle.setPrecioUnitario(dto.getPrecioUnitario());
        return detalle;
    }

    private DetallePedidoDTO.PedidoResumenDTO toPedidoDTO(Pedido pedido) {
        return DetallePedidoDTO.PedidoResumenDTO.builder()
                .idPedido(pedido.getIdPedido())
                .fechaPedido(pedido.getFechaPedido() != null ? pedido.getFechaPedido().format(DATE_TIME_FORMATTER) : null)
                .total(pedido.getTotal())
                .estado(pedido.getEstado() != null ? pedido.getEstado().name() : null)
                .cliente(toClienteDTO(pedido.getCliente()))
                .usuario(toUsuarioDTO(pedido.getUsuario()))
                .build();
    }

    private DetallePedidoDTO.ClienteResumenDTO toClienteDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        return DetallePedidoDTO.ClienteResumenDTO.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .dni(cliente.getDni())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .build();
    }

    private DetallePedidoDTO.UsuarioResumenDTO toUsuarioDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return DetallePedidoDTO.UsuarioResumenDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .build();
    }

    private DetallePedidoDTO.ProductoResumenDTO toProductoDTO(Producto producto) {
        return DetallePedidoDTO.ProductoResumenDTO.builder()
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
}
