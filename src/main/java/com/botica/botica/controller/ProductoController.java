package com.botica.botica.controller;

import com.botica.botica.dto.PageResponseDTO;
import com.botica.botica.dto.ProductoDTO;
import com.botica.botica.entity.Producto;
import com.botica.botica.mapper.ProductoMapper;
import com.botica.botica.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    @Operation(summary = "Obtener todos los productos", description = "Retorna una lista de todos los productos registrados")
    public ResponseEntity<List<ProductoDTO>> getAllProductos() {
        List<Producto> productos = productoService.findAll();
        List<ProductoDTO> dtos = productos.stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener productos paginados", description = "Retorna una lista paginada de productos registrados")
    public ResponseEntity<PageResponseDTO<ProductoDTO>> getProductosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idProducto") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageResponseDTO<ProductoDTO> response = PageResponseDTO.from(
                productoService.findAll(PageRequest.of(page, size, sort)).map(productoMapper::toDTO)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Retorna los detalles de un producto especifico")
    public ResponseEntity<ProductoDTO> getProductoById(@PathVariable Integer id) {
        Producto producto = productoService.findById(id);
        return ResponseEntity.ok(productoMapper.toDTO(producto));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar productos por nombre", description = "Retorna una lista de productos cuyo nombre coincide con el criterio de busqueda")
    public ResponseEntity<List<ProductoDTO>> searchProductos(@RequestParam String nombre) {
        List<Producto> productos = productoService.findByNombre(nombre);
        List<ProductoDTO> dtos = productos.stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/buscar/{nombre}")
    @Operation(summary = "Buscar productos por nombre en ruta", description = "Retorna una lista de productos usando el nombre recibido como parametro de ruta")
    public ResponseEntity<List<ProductoDTO>> searchProductosByPath(@PathVariable String nombre) {
        return searchProductos(nombre);
    }

    @GetMapping("/codigo-barras/{codigoBarras}")
    @Operation(summary = "Buscar producto por codigo de barras", description = "Retorna los detalles de un producto usando su codigo de barras para soporte de escaneo")
    public ResponseEntity<ProductoDTO> getProductoByCodigoBarras(@PathVariable String codigoBarras) {
        Producto producto = productoService.findByCodigoBarras(codigoBarras);
        return ResponseEntity.ok(productoMapper.toDTO(producto));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo producto", description = "Crea un nuevo producto asociado a una categoria y un proveedor")
    public ResponseEntity<ProductoDTO> createProducto(@Valid @RequestBody ProductoDTO productoDTO) {
        Producto saved = productoService.saveFromDto(productoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto existente")
    public ResponseEntity<ProductoDTO> updateProducto(@PathVariable Integer id, @Valid @RequestBody ProductoDTO productoDTO) {
        productoDTO.setIdProducto(id);
        Producto saved = productoService.saveFromDto(productoDTO);
        return ResponseEntity.ok(productoMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto", description = "Elimina un producto registrado por su identificador")
    public ResponseEntity<Void> deleteProducto(@PathVariable Integer id) {
        productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
