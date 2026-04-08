package com.botica.botica.controller;

import com.botica.botica.dto.ProveedorDTO;
import com.botica.botica.entity.Proveedor;
import com.botica.botica.mapper.ProveedorMapper;
import com.botica.botica.service.ProveedorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Operaciones CRUD para proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;
    private final ProveedorMapper proveedorMapper;

    @GetMapping
    public ResponseEntity<List<ProveedorDTO>> getAllProveedores() {
        List<Proveedor> proveedores = proveedorService.findAll();
        List<ProveedorDTO> dtos = proveedores.stream()
                .map(proveedorMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorDTO> getProveedorById(@PathVariable Integer id) {
        Proveedor proveedor = proveedorService.findById(id);
        return ResponseEntity.ok(proveedorMapper.toDTO(proveedor));
    }

    @PostMapping
    public ResponseEntity<ProveedorDTO> createProveedor(@Valid @RequestBody ProveedorDTO proveedorDTO) {
        Proveedor proveedor = proveedorMapper.toEntity(proveedorDTO);
        Proveedor saved = proveedorService.save(proveedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorDTO> updateProveedor(@PathVariable Integer id, @Valid @RequestBody ProveedorDTO proveedorDTO) {
        Proveedor existing = proveedorService.findById(id);
        Proveedor updated = proveedorMapper.updateEntity(proveedorDTO, existing);
        Proveedor saved = proveedorService.save(updated);
        return ResponseEntity.ok(proveedorMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProveedor(@PathVariable Integer id) {
        proveedorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
