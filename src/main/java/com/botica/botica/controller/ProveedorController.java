package com.botica.botica.controller;

import com.botica.botica.dto.PageResponseDTO;
import com.botica.botica.dto.ProveedorDTO;
import com.botica.botica.entity.Proveedor;
import com.botica.botica.mapper.ProveedorMapper;
import com.botica.botica.service.ProveedorService;
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
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Operaciones CRUD para proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;
    private final ProveedorMapper proveedorMapper;

    @GetMapping
    @Operation(summary = "Obtener todos los proveedores", description = "Retorna una lista de todos los proveedores registrados")
    public ResponseEntity<List<ProveedorDTO>> getAllProveedores() {
        List<Proveedor> proveedores = proveedorService.findAll();
        List<ProveedorDTO> dtos = proveedores.stream()
                .map(proveedorMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener proveedores paginados", description = "Retorna una lista paginada de proveedores registrados")
    public ResponseEntity<PageResponseDTO<ProveedorDTO>> getProveedoresPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idProveedor") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageResponseDTO<ProveedorDTO> response = PageResponseDTO.from(
                proveedorService.findAll(PageRequest.of(page, size, sort)).map(proveedorMapper::toDTO)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor por ID", description = "Retorna los detalles de un proveedor especifico")
    public ResponseEntity<ProveedorDTO> getProveedorById(@PathVariable Integer id) {
        Proveedor proveedor = proveedorService.findById(id);
        return ResponseEntity.ok(proveedorMapper.toDTO(proveedor));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo proveedor", description = "Crea un nuevo proveedor en el sistema")
    public ResponseEntity<ProveedorDTO> createProveedor(@Valid @RequestBody ProveedorDTO proveedorDTO) {
        Proveedor proveedor = proveedorMapper.toEntity(proveedorDTO);
        Proveedor saved = proveedorService.save(proveedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proveedor", description = "Actualiza los datos de un proveedor existente")
    public ResponseEntity<ProveedorDTO> updateProveedor(@PathVariable Integer id, @Valid @RequestBody ProveedorDTO proveedorDTO) {
        Proveedor existing = proveedorService.findById(id);
        Proveedor updated = proveedorMapper.updateEntity(proveedorDTO, existing);
        Proveedor saved = proveedorService.save(updated);
        return ResponseEntity.ok(proveedorMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proveedor", description = "Elimina un proveedor registrado por su identificador")
    public ResponseEntity<Void> deleteProveedor(@PathVariable Integer id) {
        proveedorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
