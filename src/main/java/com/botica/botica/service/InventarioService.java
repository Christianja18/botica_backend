package com.botica.botica.service;

import com.botica.botica.dto.InventarioDTO;
import com.botica.botica.entity.Inventario;
import com.botica.botica.entity.Producto;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.InventarioRepository;
import com.botica.botica.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;

    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    public Inventario findById(Integer id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado con id: " + id));
    }

    public Inventario saveFromDto(InventarioDTO dto) {
        Inventario inventario = dto.getIdInventario() != null
                ? findById(dto.getIdInventario())
                : new Inventario();

        Producto producto = productoRepository.findById(dto.getIdProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + dto.getIdProducto()));

        inventario.setProducto(producto);
        inventario.setStockActual(dto.getStockActual());
        inventario.setStockMinimo(dto.getStockMinimo());
        inventario.setFechaActualizacion(LocalDateTime.now());

        Inventario saved = inventarioRepository.save(inventario);
        return findById(saved.getIdInventario());
    }

    public void deleteById(Integer id) {
        if (!inventarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inventario no encontrado con id: " + id);
        }
        inventarioRepository.deleteById(id);
    }
}
