package com.botica.botica.service;

import com.botica.botica.dto.ProductoDTO;
import com.botica.botica.entity.Categoria;
import com.botica.botica.entity.Producto;
import com.botica.botica.entity.Proveedor;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.exception.ProductoNotFoundException;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.CategoriaRepository;
import com.botica.botica.repository.ProductoRepository;
import com.botica.botica.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);
    private static final DateTimeFormatter[] FECHA_VENCIMIENTO_FORMATTERS = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    };

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public Page<Producto> findAll(Pageable pageable) {
        return productoRepository.findAll(pageable);
    }

    public Producto findById(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con id: " + id));
    }

    public List<Producto> findByNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Producto findByCodigoBarras(String codigoBarras) {
        return productoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con codigo de barras: " + codigoBarras));
    }

    public Producto saveFromDto(ProductoDTO dto) {
        Producto producto = dto.getIdProducto() != null
                ? findById(dto.getIdProducto())
                : new Producto();

        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id: " + dto.getIdCategoria()));
        Proveedor proveedor = proveedorRepository.findById(dto.getIdProveedor())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + dto.getIdProveedor()));

        producto.setNombre(dto.getNombre());
        producto.setCodigoBarras(dto.getCodigoBarras());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setPrecioCompra(dto.getPrecioCompra());
        producto.setRequiereReceta(dto.getRequiereReceta());
        producto.setFechaVencimiento(parseFechaVencimiento(dto.getFechaVencimiento()));
        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);

        if (producto.getFechaCreacion() == null) {
            producto.setFechaCreacion(LocalDateTime.now());
        }

        return save(producto);
    }

    public Producto save(Producto producto) {
        logger.info("Guardando producto: nombre={}, precioVenta={}, precioCompra={}",
                    producto.getNombre(), producto.getPrecioVenta(), producto.getPrecioCompra());
        try {
            validateCodigoBarras(producto);
            Producto saved = productoRepository.save(producto);
            logger.info("Producto guardado exitosamente con id: {}", saved.getIdProducto());
            return findById(saved.getIdProducto());
        } catch (Exception e) {
            logger.error("Error al guardar producto: nombre={}, precioVenta={}, error={}",
                        producto.getNombre(), producto.getPrecioVenta(), e.getMessage());
            throw e;
        }
    }

    public void deleteById(Integer id) {
        if (!productoRepository.existsById(id)) {
            throw new ProductoNotFoundException("Producto no encontrado con id: " + id);
        }
        productoRepository.deleteById(id);
    }

    private void validateCodigoBarras(Producto producto) {
        productoRepository.findByCodigoBarras(producto.getCodigoBarras())
                .filter(existing -> producto.getIdProducto() == null || !existing.getIdProducto().equals(producto.getIdProducto()))
                .ifPresent(existing -> {
                    throw new BadRequestException("Codigo de barras ya existe: " + producto.getCodigoBarras());
                });
    }

    private LocalDate parseFechaVencimiento(String fechaVencimiento) {
        if (fechaVencimiento == null || fechaVencimiento.isBlank()) {
            return null;
        }

        for (DateTimeFormatter formatter : FECHA_VENCIMIENTO_FORMATTERS) {
            try {
                return LocalDate.parse(fechaVencimiento, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next format.
            }
        }

        throw new BadRequestException("Formato de fecha de vencimiento invalido. Use yyyy-MM-dd o dd/MM/yyyy");
    }
}
