package com.botica.botica.controller;

import com.botica.botica.dto.ImportResultDTO;
import com.botica.botica.dto.PageResponseDTO;
import com.botica.botica.dto.UsuarioDTO;
import com.botica.botica.entity.Usuario;
import com.botica.botica.mapper.UsuarioMapper;
import com.botica.botica.service.UsuarioService;
import com.botica.botica.service.importexport.ImportExportFacadeService;
import com.botica.botica.service.importexport.TabularFileFormat;
import com.botica.botica.util.ImportExportResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Operaciones CRUD para usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;
    private final ImportExportFacadeService importExportFacadeService;

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

    @GetMapping("/paginado")
    @Operation(summary = "Obtener usuarios paginados", description = "Retorna una lista paginada de usuarios registrados")
    public ResponseEntity<PageResponseDTO<UsuarioDTO>> getUsuariosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idUsuario") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageResponseDTO<UsuarioDTO> response = PageResponseDTO.from(
                usuarioService.findAll(PageRequest.of(page, size, sort)).map(usuarioMapper::toDTO)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exportar/{formato}")
    @Operation(summary = "Exportar usuarios", description = "Exporta los usuarios registrados en formato CSV o Excel usando un archivo reimportable sin exponer hashes de contrasena")
    public ResponseEntity<byte[]> exportUsuarios(@PathVariable String formato) {
        return ImportExportResponseBuilder.build(
                importExportFacadeService.exportData("usuarios", TabularFileFormat.from(formato))
        );
    }

    @PostMapping(value = "/importar/{formato}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar usuarios", description = "Importa usuarios desde un archivo CSV o Excel. Inserta o actualiza por id_usuario o email, resolviendo el rol por id o nombre")
    public ResponseEntity<ImportResultDTO> importUsuarios(@PathVariable String formato,
                                                          @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                importExportFacadeService.importData("usuarios", file, TabularFileFormat.from(formato))
        );
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
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario registrado por su identificador")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Integer id) {
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
