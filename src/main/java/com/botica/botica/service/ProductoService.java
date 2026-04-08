package com.botica.botica.service;

import com.botica.botica.entity.Producto;
import com.botica.botica.exception.ProductoNotFoundException;
import com.botica.botica.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public Producto findById(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con id: " + id));
    }

    public List<Producto> findByNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Producto save(Producto producto) {
        logger.info("Guardando producto: nombre={}, precioVenta={}, precioCompra={}",
                    producto.getNombre(), producto.getPrecioVenta(), producto.getPrecioCompra());
        try {
            Producto saved = productoRepository.save(producto);
            logger.info("Producto guardado exitosamente con id: {}", saved.getIdProducto());
            return saved;
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
}