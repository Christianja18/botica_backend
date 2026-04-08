package com.botica.botica.controller;

import com.botica.botica.dto.RolDTO;
import com.botica.botica.entity.Rol;
import com.botica.botica.mapper.RolMapper;
import com.botica.botica.service.RolService;
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
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Operaciones CRUD para roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;
    private final RolMapper rolMapper;

    @GetMapping
    @Operation(summary = "Obtener todos los roles", description = "Retorna una lista de todos los roles registrados")
    public ResponseEntity<List<RolDTO>> getAllRoles() {
        return ResponseEntity.ok(rolService.findAll().stream()
                .map(rolMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID", description = "Retorna los detalles de un rol especifico")
    public ResponseEntity<RolDTO> getRolById(@PathVariable Integer id) {
        return ResponseEntity.ok(rolMapper.toDTO(rolService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo rol", description = "Crea un nuevo rol con sus permisos asociados")
    public ResponseEntity<RolDTO> createRol(@Valid @RequestBody RolDTO rolDTO) {
        Rol saved = rolService.save(rolMapper.toEntity(rolDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(rolMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol", description = "Actualiza los datos y permisos de un rol existente")
    public ResponseEntity<RolDTO> updateRol(@PathVariable Integer id, @Valid @RequestBody RolDTO rolDTO) {
        Rol existing = rolService.findById(id);
        Rol updated = rolMapper.updateEntity(rolDTO, existing);
        return ResponseEntity.ok(rolMapper.toDTO(rolService.save(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol", description = "Elimina un rol registrado por su identificador")
    public ResponseEntity<Void> deleteRol(@PathVariable Integer id) {
        rolService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
