package com.botica.botica.controller;

import com.botica.botica.dto.ProductoDTO;
import com.botica.botica.entity.Producto;
import com.botica.botica.mapper.ProductoMapper;
import com.botica.botica.service.ProductoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Operaciones CRUD para productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoMapper productoMapper;

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> getAllProductos() {
        List<Producto> productos = productoService.findAll();
        List<ProductoDTO> dtos = productos.stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> getProductoById(@PathVariable Integer id) {
        Producto producto = productoService.findById(id);
        return ResponseEntity.ok(productoMapper.toDTO(producto));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductoDTO>> searchProductos(@RequestParam String nombre) {
        List<Producto> productos = productoService.findByNombre(nombre);
        List<ProductoDTO> dtos = productos.stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/buscar/{nombre}")
    public ResponseEntity<List<ProductoDTO>> searchProductosByPath(@PathVariable String nombre) {
        return searchProductos(nombre);
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> createProducto(@Valid @RequestBody ProductoDTO productoDTO) {
        Producto saved = productoService.saveFromDto(productoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> updateProducto(@PathVariable Integer id, @Valid @RequestBody ProductoDTO productoDTO) {
        productoDTO.setIdProducto(id);
        Producto saved = productoService.saveFromDto(productoDTO);
        return ResponseEntity.ok(productoMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable Integer id) {
        productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
