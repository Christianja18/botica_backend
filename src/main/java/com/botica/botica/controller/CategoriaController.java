package com.botica.botica.controller;

import com.botica.botica.dto.CategoriaDTO;
import com.botica.botica.dto.ImportResultDTO;
import com.botica.botica.dto.PageResponseDTO;
import com.botica.botica.entity.Categoria;
import com.botica.botica.mapper.CategoriaMapper;
import com.botica.botica.service.CategoriaService;
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
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Operaciones CRUD para categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaMapper categoriaMapper;
    private final ImportExportFacadeService importExportFacadeService;

    @GetMapping
    @Operation(summary = "Obtener todas las categorias", description = "Retorna una lista de todas las categorias de productos registradas")
    public ResponseEntity<List<CategoriaDTO>> getAllCategorias() {
        return ResponseEntity.ok(categoriaService.findAll().stream()
                .map(categoriaMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener categorias paginadas", description = "Retorna una lista paginada de categorias registradas")
    public ResponseEntity<PageResponseDTO<CategoriaDTO>> getCategoriasPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idCategoria") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageResponseDTO<CategoriaDTO> response = PageResponseDTO.from(
                categoriaService.findAll(PageRequest.of(page, size, sort)).map(categoriaMapper::toDTO)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exportar/{formato}")
    @Operation(summary = "Exportar categorias", description = "Exporta las categorias registradas en formato CSV o Excel usando un archivo reimportable")
    public ResponseEntity<byte[]> exportCategorias(@PathVariable String formato) {
        return ImportExportResponseBuilder.build(
                importExportFacadeService.exportData("categorias", TabularFileFormat.from(formato))
        );
    }

    @PostMapping(value = "/importar/{formato}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar categorias", description = "Importa categorias desde un archivo CSV o Excel. Inserta o actualiza por id_categoria o nombre")
    public ResponseEntity<ImportResultDTO> importCategorias(@PathVariable String formato,
                                                            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                importExportFacadeService.importData("categorias", file, TabularFileFormat.from(formato))
        );
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
