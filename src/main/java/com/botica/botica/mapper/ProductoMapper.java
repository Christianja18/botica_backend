package com.botica.botica.mapper;

import com.botica.botica.dto.ProductoDTO;
import com.botica.botica.entity.Producto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ProductoMapper {

    private final ModelMapper modelMapper;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ProductoDTO toDTO(Producto producto) {
        if (producto == null) {
            return null;
        }
        ProductoDTO dto = modelMapper.map(producto, ProductoDTO.class);
        if (producto.getFechaCreacion() != null) {
            dto.setFechaCreacion(producto.getFechaCreacion().format(dateFormatter));
        }
        // Mapear IDs de relaciones
        if (producto.getCategoria() != null) {
            dto.setIdCategoria(producto.getCategoria().getIdCategoria());
        }
        if (producto.getProveedor() != null) {
            dto.setIdProveedor(producto.getProveedor().getIdProveedor());
        }
        return dto;
    }

    public Producto toEntity(ProductoDTO dto) {
        if (dto == null) {
            return null;
        }
        Producto producto = new Producto();
        producto.setIdProducto(dto.getIdProducto());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setPrecioCompra(dto.getPrecioCompra());
        producto.setRequiereReceta(dto.getRequiereReceta());
        if (producto.getFechaCreacion() == null) {
            producto.setFechaCreacion(LocalDateTime.now());
        }
        return producto;
    }

    public Producto updateEntity(ProductoDTO dto, Producto producto) {
        if (dto == null) {
            return producto;
        }
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setPrecioCompra(dto.getPrecioCompra());
        producto.setRequiereReceta(dto.getRequiereReceta());
        return producto;
    }
}
