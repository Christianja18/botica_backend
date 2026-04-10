package com.botica.botica.controller;

import com.botica.botica.dto.ImportResultDTO;
import com.botica.botica.dto.InventarioDTO;
import com.botica.botica.dto.PageResponseDTO;
import com.botica.botica.entity.Inventario;
import com.botica.botica.mapper.InventarioMapper;
import com.botica.botica.service.InventarioService;
import com.botica.botica.service.importexport.ImportExportFacadeService;
import com.botica.botica.service.importexport.TabularFileFormat;
import com.botica.botica.util.ImportExportResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Operaciones CRUD para inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final InventarioMapper inventarioMapper;
    private final ImportExportFacadeService importExportFacadeService;

    @GetMapping
    @Operation(summary = "Obtener todo el inventario", description = "Retorna una lista de todos los registros de inventario")
    public ResponseEntity<List<InventarioDTO>> getAllInventario() {
        return ResponseEntity.ok(inventarioService.findAll().stream()
                .map(inventarioMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener inventario paginado", description = "Retorna una lista paginada de registros de inventario")
    public ResponseEntity<PageResponseDTO<InventarioDTO>> getInventarioPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idInventario") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageResponseDTO<InventarioDTO> response = PageResponseDTO.from(
                inventarioService.findAll(PageRequest.of(page, size, sort)).map(inventarioMapper::toDTO)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exportar/{formato}")
    @Operation(summary = "Exportar inventario", description = "Exporta el inventario registrado en formato CSV o Excel usando un archivo reimportable")
    public ResponseEntity<byte[]> exportInventario(@PathVariable String formato) {
        return ImportExportResponseBuilder.build(
                importExportFacadeService.exportData("inventario", TabularFileFormat.from(formato))
        );
    }

    @PostMapping(value = "/importar/{formato}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar inventario", description = "Importa inventario desde un archivo CSV o Excel. Inserta o actualiza por id_inventario o producto asociado")
    public ResponseEntity<ImportResultDTO> importInventario(@PathVariable String formato,
                                                            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                importExportFacadeService.importData("inventario", file, TabularFileFormat.from(formato))
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener inventario por ID", description = "Retorna los detalles de un registro de inventario especifico")
    public ResponseEntity<InventarioDTO> getInventarioById(@PathVariable Integer id) {
        return ResponseEntity.ok(inventarioMapper.toDTO(inventarioService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo registro de inventario", description = "Crea un nuevo registro de inventario para un producto")
    public ResponseEntity<InventarioDTO> createInventario(@Valid @RequestBody InventarioDTO inventarioDTO) {
        Inventario saved = inventarioService.saveFromDto(inventarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar inventario", description = "Actualiza los datos de un registro de inventario existente")
    public ResponseEntity<InventarioDTO> updateInventario(@PathVariable Integer id, @Valid @RequestBody InventarioDTO inventarioDTO) {
        inventarioDTO.setIdInventario(id);
        Inventario saved = inventarioService.saveFromDto(inventarioDTO);
        return ResponseEntity.ok(inventarioMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar inventario", description = "Elimina un registro de inventario por su identificador")
    public ResponseEntity<Void> deleteInventario(@PathVariable Integer id) {
        inventarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
