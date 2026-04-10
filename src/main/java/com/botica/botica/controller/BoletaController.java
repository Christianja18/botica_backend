package com.botica.botica.controller;

import com.botica.botica.dto.BoletaDTO;
import com.botica.botica.dto.ImportResultDTO;
import com.botica.botica.dto.PageResponseDTO;
import com.botica.botica.entity.Boleta;
import com.botica.botica.mapper.BoletaMapper;
import com.botica.botica.service.BoletaService;
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
@RequestMapping("/api/boletas")
@Tag(name = "Boletas", description = "Operaciones CRUD para boletas")
@RequiredArgsConstructor
public class BoletaController {

    private final BoletaService boletaService;
    private final BoletaMapper boletaMapper;
    private final ImportExportFacadeService importExportFacadeService;

    @GetMapping
    @Operation(summary = "Obtener todas las boletas", description = "Retorna una lista de todas las boletas registradas en el sistema")
    public ResponseEntity<List<BoletaDTO>> getAllBoletas() {
        return ResponseEntity.ok(boletaService.findAll().stream()
                .map(boletaMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener boletas paginadas", description = "Retorna una lista paginada de boletas registradas")
    public ResponseEntity<PageResponseDTO<BoletaDTO>> getBoletasPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idBoleta") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageResponseDTO<BoletaDTO> response = PageResponseDTO.from(
                boletaService.findAll(PageRequest.of(page, size, sort)).map(boletaMapper::toDTO)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exportar/{formato}")
    @Operation(summary = "Exportar boletas", description = "Exporta las boletas registradas en formato CSV o Excel usando un archivo reimportable")
    public ResponseEntity<byte[]> exportBoletas(@PathVariable String formato) {
        return ImportExportResponseBuilder.build(
                importExportFacadeService.exportData("boletas", TabularFileFormat.from(formato))
        );
    }

    @PostMapping(value = "/importar/{formato}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar boletas", description = "Importa boletas desde un archivo CSV o Excel. Inserta o actualiza por id_boleta o numero_boleta")
    public ResponseEntity<ImportResultDTO> importBoletas(@PathVariable String formato,
                                                         @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                importExportFacadeService.importData("boletas", file, TabularFileFormat.from(formato))
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener boleta por ID", description = "Retorna los detalles de una boleta especifica")
    public ResponseEntity<BoletaDTO> getBoletaById(@PathVariable Integer id) {
        return ResponseEntity.ok(boletaMapper.toDTO(boletaService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear nueva boleta", description = "Crea una nueva boleta asociada a un pedido existente")
    public ResponseEntity<BoletaDTO> createBoleta(@Valid @RequestBody BoletaDTO boletaDTO) {
        Boleta saved = boletaService.saveFromDto(boletaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(boletaMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar boleta", description = "Actualiza los datos de una boleta existente")
    public ResponseEntity<BoletaDTO> updateBoleta(@PathVariable Integer id, @Valid @RequestBody BoletaDTO boletaDTO) {
        boletaDTO.setIdBoleta(id);
        Boleta saved = boletaService.saveFromDto(boletaDTO);
        return ResponseEntity.ok(boletaMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar boleta", description = "Elimina una boleta registrada por su identificador")
    public ResponseEntity<Void> deleteBoleta(@PathVariable Integer id) {
        boletaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
