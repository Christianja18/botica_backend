package com.botica.botica.service;

import com.botica.botica.dto.DetallePedidoDTO;
import com.botica.botica.dto.PedidoDTO;
import com.botica.botica.entity.Cliente;
import com.botica.botica.entity.DetallePedido;
import com.botica.botica.entity.Pedido;
import com.botica.botica.entity.Producto;
import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.ClienteRepository;
import com.botica.botica.repository.PedidoRepository;
import com.botica.botica.repository.ProductoRepository;
import com.botica.botica.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    public Pedido findById(Integer id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
    }

    public List<Pedido> findByEstado(Pedido.EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }

    public Pedido save(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido saveFromDto(PedidoDTO dto) {
        boolean creating = dto.getIdPedido() == null;
        Pedido pedido = creating ? new Pedido() : findById(dto.getIdPedido());

        pedido.setCliente(resolveCliente(dto.getIdCliente()));
        pedido.setUsuario(resolveUsuario(dto.getIdUsuario()));
        pedido.setEstado(resolveEstado(dto.getEstado()));

        if (creating && dto.getFechaPedido() != null && !dto.getFechaPedido().isBlank()) {
            pedido.setFechaPedido(parseFecha(dto.getFechaPedido()));
        }

        if (dto.getDetalles() != null) {
            List<DetallePedido> detalles = synchronizeDetalles(dto.getDetalles(), pedido);
            if (pedido.getDetalles() == null) {
                pedido.setDetalles(new ArrayList<>());
            } else {
                pedido.getDetalles().clear();
            }
            pedido.getDetalles().addAll(detalles);
            pedido.setTotal(calculateTotal(pedido.getDetalles()));
        } else if (creating) {
            pedido.setDetalles(new ArrayList<>());
            pedido.setTotal(BigDecimal.ZERO);
        }

        validatePedidoForSingleOperation(pedido, creating, dto.getDetalles() != null);

        Pedido saved = pedidoRepository.save(pedido);
        return findById(saved.getIdPedido());
    }

    public void deleteById(Integer id) {
        if (!pedidoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pedido no encontrado con id: " + id);
        }
        pedidoRepository.deleteById(id);
    }

    private Cliente resolveCliente(Integer idCliente) {
        if (idCliente == null) {
            return null;
        }
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + idCliente));
    }

    private Usuario resolveUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + idUsuario));
    }

    private Producto resolveProducto(Integer idProducto) {
        return productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + idProducto));
    }

    private Pedido.EstadoPedido resolveEstado(String estado) {
        try {
            return estado == null ? Pedido.EstadoPedido.pendiente : Pedido.EstadoPedido.valueOf(estado);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Estado de pedido invalido: " + estado);
        }
    }

    private LocalDateTime parseFecha(String fechaPedido) {
        try {
            return LocalDateTime.parse(fechaPedido, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Formato de fecha invalido. Use dd/MM/yyyy HH:mm");
        }
    }

    private List<DetallePedido> synchronizeDetalles(List<DetallePedidoDTO> detalleDTOs, Pedido pedido) {
        Map<Integer, DetallePedido> existentes = new HashMap<>();
        if (pedido.getDetalles() != null) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                if (detalle.getIdDetalle() != null) {
                    existentes.put(detalle.getIdDetalle(), detalle);
                }
            }
        }

        return new ArrayList<>(detalleDTOs.stream()
                .map(dto -> buildDetalle(dto, pedido, existentes))
                .toList());
    }

    private DetallePedido buildDetalle(DetallePedidoDTO dto, Pedido pedido, Map<Integer, DetallePedido> existentes) {
        DetallePedido detalle;

        if (dto.getIdDetalle() != null) {
            detalle = existentes.get(dto.getIdDetalle());
            if (detalle == null) {
                throw new BadRequestException("El detalle con id " + dto.getIdDetalle() + " no pertenece al pedido actual");
            }
        } else {
            detalle = new DetallePedido();
        }

        detalle.setPedido(pedido);
        detalle.setProducto(resolveProducto(dto.getIdProducto()));
        detalle.setCantidad(dto.getCantidad());
        detalle.setPrecioUnitario(dto.getPrecioUnitario());
        return detalle;
    }

    private void validatePedidoForSingleOperation(Pedido pedido, boolean creating, boolean detallesWereProvided) {
        if (pedido.getEstado() == Pedido.EstadoPedido.completado) {
            if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
                throw new BadRequestException("Un pedido completado debe incluir al menos un detalle");
            }
        }

        if (creating && detallesWereProvided && pedido.getDetalles().stream().anyMatch(detalle -> detalle.getCantidad() == null || detalle.getCantidad() <= 0)) {
            throw new BadRequestException("Todos los detalles del pedido deben tener una cantidad valida");
        }
    }

    private BigDecimal calculateTotal(List<DetallePedido> detalles) {
        return detalles.stream()
                .map(detalle -> detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
