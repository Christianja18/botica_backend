package com.botica.botica.mapper;

import com.botica.botica.dto.InventarioDTO;
import com.botica.botica.entity.Inventario;
import com.botica.botica.entity.Producto;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class InventarioMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

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
            dto.setProducto(toProductoDTO(inventario.getProducto()));
        }

        if (inventario.getFechaActualizacion() != null) {
            dto.setFechaActualizacion(inventario.getFechaActualizacion().format(DATE_TIME_FORMATTER));
        }

        return dto;
    }

    private InventarioDTO.ProductoResumenDTO toProductoDTO(Producto producto) {
        return InventarioDTO.ProductoResumenDTO.builder()
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
}
