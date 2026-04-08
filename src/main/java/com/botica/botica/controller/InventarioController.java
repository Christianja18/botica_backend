package com.botica.botica.controller;

import com.botica.botica.dto.InventarioDTO;
import com.botica.botica.entity.Inventario;
import com.botica.botica.mapper.InventarioMapper;
import com.botica.botica.service.InventarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Operaciones CRUD para inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final InventarioMapper inventarioMapper;

    @GetMapping
    public ResponseEntity<List<InventarioDTO>> getAllInventario() {
        return ResponseEntity.ok(inventarioService.findAll().stream()
                .map(inventarioMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioDTO> getInventarioById(@PathVariable Integer id) {
        return ResponseEntity.ok(inventarioMapper.toDTO(inventarioService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<InventarioDTO> createInventario(@Valid @RequestBody InventarioDTO inventarioDTO) {
        Inventario saved = inventarioService.saveFromDto(inventarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventarioDTO> updateInventario(@PathVariable Integer id, @Valid @RequestBody InventarioDTO inventarioDTO) {
        inventarioDTO.setIdInventario(id);
        Inventario saved = inventarioService.saveFromDto(inventarioDTO);
        return ResponseEntity.ok(inventarioMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventario(@PathVariable Integer id) {
        inventarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
