package com.botica.botica.controller;

import com.botica.botica.dto.CategoriaDTO;
import com.botica.botica.entity.Categoria;
import com.botica.botica.mapper.CategoriaMapper;
import com.botica.botica.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Operaciones CRUD para categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaMapper categoriaMapper;

    @GetMapping
    @Operation(summary = "Obtener todas las categorias", description = "Retorna una lista de todas las categorias de productos registradas")
    public ResponseEntity<List<CategoriaDTO>> getAllCategorias() {
        return ResponseEntity.ok(categoriaService.findAll().stream()
                .map(categoriaMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoria por ID", description = "Retorna los detalles de una categoria especifica")
    public ResponseEntity<CategoriaDTO> getCategoriaById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaMapper.toDTO(categoriaService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear nueva categoria", description = "Crea una nueva categoria para clasificar productos")
    public ResponseEntity<CategoriaDTO> createCategoria(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        Categoria saved = categoriaService.save(categoriaMapper.toEntity(categoriaDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoria", description = "Actualiza los datos de una categoria existente")
    public ResponseEntity<CategoriaDTO> updateCategoria(@PathVariable Integer id, @Valid @RequestBody CategoriaDTO categoriaDTO) {
        Categoria existing = categoriaService.findById(id);
        Categoria updated = categoriaMapper.updateEntity(categoriaDTO, existing);
        return ResponseEntity.ok(categoriaMapper.toDTO(categoriaService.save(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoria", description = "Elimina una categoria registrada por su identificador")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Integer id) {
        categoriaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
