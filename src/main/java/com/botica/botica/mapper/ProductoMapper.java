package com.botica.botica.mapper;

import com.botica.botica.dto.ProductoDTO;
import com.botica.botica.entity.Categoria;
import com.botica.botica.entity.Producto;
import com.botica.botica.entity.Proveedor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ProductoMapper {

    private final ModelMapper modelMapper;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public ProductoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ProductoDTO toDTO(Producto producto) {
        if (producto == null) {
            return null;
        }
        ProductoDTO dto = modelMapper.map(producto, ProductoDTO.class);
        if (producto.getFechaCreacion() != null) {
            dto.setFechaCreacion(producto.getFechaCreacion().format(dateFormatter));
        }
        if (producto.getFechaVencimiento() != null) {
            dto.setFechaVencimiento(producto.getFechaVencimiento().format(dateOnlyFormatter));
        }
        if (producto.getCategoria() != null) {
            dto.setIdCategoria(producto.getCategoria().getIdCategoria());
            dto.setCategoria(toCategoriaDTO(producto.getCategoria()));
        }
        if (producto.getProveedor() != null) {
            dto.setIdProveedor(producto.getProveedor().getIdProveedor());
            dto.setProveedor(toProveedorDTO(producto.getProveedor()));
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
        producto.setCodigoBarras(dto.getCodigoBarras());
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
        producto.setCodigoBarras(dto.getCodigoBarras());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setPrecioCompra(dto.getPrecioCompra());
        producto.setRequiereReceta(dto.getRequiereReceta());
        return producto;
    }

    private ProductoDTO.CategoriaResumenDTO toCategoriaDTO(Categoria categoria) {
        return ProductoDTO.CategoriaResumenDTO.builder()
                .idCategoria(categoria.getIdCategoria())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }

    private ProductoDTO.ProveedorResumenDTO toProveedorDTO(Proveedor proveedor) {
        return ProductoDTO.ProveedorResumenDTO.builder()
                .idProveedor(proveedor.getIdProveedor())
                .nombre(proveedor.getNombre())
                .ruc(proveedor.getRuc())
                .telefono(proveedor.getTelefono())
                .email(proveedor.getEmail())
                .direccion(proveedor.getDireccion())
                .build();
    }
}
