package com.botica.botica.mapper;

import com.botica.botica.dto.PedidoDTO;
import com.botica.botica.entity.Pedido;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PedidoMapper {

    private final ModelMapper modelMapper;
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
        }

        if (pedido.getUsuario() != null) {
            dto.setIdUsuario(pedido.getUsuario().getIdUsuario());
        }

        if (pedido.getFechaPedido() != null) {
            dto.setFechaPedido(pedido.getFechaPedido().format(dateFormatter));
        }

        if (pedido.getDetalles() != null) {
            dto.setDetalles(pedido.getDetalles().stream()
                    .map(detallePedidoMapper::toDTO)
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
        pedido.setTotal(dto.getTotal());

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
        pedido.setTotal(dto.getTotal());

        if (dto.getEstado() != null) {
            pedido.setEstado(Pedido.EstadoPedido.valueOf(dto.getEstado()));
        }

        return pedido;
    }
}
