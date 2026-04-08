package com.botica.botica.controller;

import com.botica.botica.dto.UsuarioDTO;
import com.botica.botica.entity.Usuario;
import com.botica.botica.mapper.UsuarioMapper;
import com.botica.botica.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Operaciones CRUD para usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios", description = "Retorna una lista de todos los usuarios registrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")
    })
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        List<UsuarioDTO> dtos = usuarioService.findAll().stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Retorna los detalles de un usuario especifico")
    public ResponseEntity<UsuarioDTO> getUsuarioById(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioMapper.toDTO(usuarioService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario", description = "Crea un nuevo usuario en el sistema")
    public ResponseEntity<UsuarioDTO> createUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        Usuario saved = usuarioService.saveFromDto(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    public ResponseEntity<UsuarioDTO> updateUsuario(@PathVariable Integer id, @Valid @RequestBody UsuarioDTO usuarioDTO) {
        usuarioDTO.setIdUsuario(id);
        return ResponseEntity.ok(usuarioMapper.toDTO(usuarioService.saveFromDto(usuarioDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Integer id) {
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
