package com.botica.botica.mapper;

import com.botica.botica.dto.PedidoDTO;
import com.botica.botica.entity.Cliente;
import com.botica.botica.entity.Pedido;
import com.botica.botica.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PedidoMapper {

    private final DetallePedidoMapper detallePedidoMapper;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PedidoDTO toDTO(Pedido pedido) {
        if (pedido == null) {
            return null;
        }
        PedidoDTO dto = PedidoDTO.builder()
                .idPedido(pedido.getIdPedido())
                .total(pedido.getTotal())
                .estado(pedido.getEstado().toString())
                .build();

        if (pedido.getCliente() != null) {
            dto.setIdCliente(pedido.getCliente().getIdCliente());
            dto.setCliente(toClienteDTO(pedido.getCliente()));
        }

        if (pedido.getUsuario() != null) {
            dto.setIdUsuario(pedido.getUsuario().getIdUsuario());
            dto.setUsuario(toUsuarioDTO(pedido.getUsuario()));
        }

        if (pedido.getFechaPedido() != null) {
            dto.setFechaPedido(pedido.getFechaPedido().format(dateFormatter));
        }

        if (pedido.getDetalles() != null) {
            dto.setDetalles(pedido.getDetalles().stream()
                    .map(detallePedidoMapper::toDTOWithoutPedido)
                    .collect(Collectors.toList()));
        } else {
            dto.setDetalles(Collections.emptyList());
        }

        return dto;
    }

    public Pedido toEntity(PedidoDTO dto) {
        if (dto == null) {
            return null;
        }
        Pedido pedido = new Pedido();
        pedido.setIdPedido(dto.getIdPedido());

        if (dto.getEstado() != null) {
            pedido.setEstado(Pedido.EstadoPedido.valueOf(dto.getEstado()));
        }

        if (pedido.getFechaPedido() == null) {
            pedido.setFechaPedido(LocalDateTime.now());
        }

        return pedido;
    }

    public Pedido updateEntity(PedidoDTO dto, Pedido pedido) {
        if (dto == null) {
            return pedido;
        }

        if (dto.getEstado() != null) {
            pedido.setEstado(Pedido.EstadoPedido.valueOf(dto.getEstado()));
        }

        return pedido;
    }

    private PedidoDTO.ClienteResumenDTO toClienteDTO(Cliente cliente) {
        return PedidoDTO.ClienteResumenDTO.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .dni(cliente.getDni())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .build();
    }

    private PedidoDTO.UsuarioResumenDTO toUsuarioDTO(Usuario usuario) {
        return PedidoDTO.UsuarioResumenDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .build();
    }
}
