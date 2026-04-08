package com.botica.botica.mapper;

import com.botica.botica.dto.InventarioDTO;
import com.botica.botica.entity.Inventario;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class InventarioMapper {

    private final ModelMapper modelMapper;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public InventarioDTO toDTO(Inventario inventario) {
        if (inventario == null) {
            return null;
        }

        InventarioDTO dto = InventarioDTO.builder()
                .idInventario(inventario.getIdInventario())
                .stockActual(inventario.getStockActual())
                .stockMinimo(inventario.getStockMinimo())
                .build();

        if (inventario.getProducto() != null) {
            dto.setIdProducto(inventario.getProducto().getIdProducto());
        }

        if (inventario.getFechaActualizacion() != null) {
            dto.setFechaActualizacion(inventario.getFechaActualizacion().format(DATE_TIME_FORMATTER));
        }

        return dto;
    }
}
