package com.botica.botica.mapper;

import com.botica.botica.dto.DetallePedidoDTO;
import com.botica.botica.entity.DetallePedido;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DetallePedidoMapper {

    private final ModelMapper modelMapper;

    public DetallePedidoDTO toDTO(DetallePedido detalle) {
        if (detalle == null) {
            return null;
        }
        DetallePedidoDTO dto = DetallePedidoDTO.builder()
                .idDetalle(detalle.getIdDetalle())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(resolveSubtotal(detalle))
                .build();

        if (detalle.getPedido() != null) {
            dto.setIdPedido(detalle.getPedido().getIdPedido());
        }

        if (detalle.getProducto() != null) {
            dto.setIdProducto(detalle.getProducto().getIdProducto());
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
