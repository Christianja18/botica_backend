package com.botica.botica.controller;

import com.botica.botica.dto.DetallePedidoDTO;
import com.botica.botica.dto.ImportResultDTO;
import com.botica.botica.dto.PageResponseDTO;
import com.botica.botica.entity.DetallePedido;
import com.botica.botica.mapper.DetallePedidoMapper;
import com.botica.botica.service.DetallePedidoService;
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
@RequestMapping("/api/detalles-pedido")
@Tag(name = "DetallePedido", description = "Operaciones CRUD para detalles de pedido")
@RequiredArgsConstructor
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;
    private final DetallePedidoMapper detallePedidoMapper;
    private final ImportExportFacadeService importExportFacadeService;

    @GetMapping
    @Operation(summary = "Obtener todos los detalles de pedido", description = "Retorna una lista de todos los detalles de pedido registrados")
    public ResponseEntity<List<DetallePedidoDTO>> getAllDetallesPedido() {
        return ResponseEntity.ok(detallePedidoService.findAll().stream()
                .map(detallePedidoMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener detalles de pedido paginados", description = "Retorna una lista paginada de lineas de detalle de pedido")
    public ResponseEntity<PageResponseDTO<DetallePedidoDTO>> getDetallesPedidoPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idDetalle") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageResponseDTO<DetallePedidoDTO> response = PageResponseDTO.from(
                detallePedidoService.findAll(PageRequest.of(page, size, sort)).map(detallePedidoMapper::toDTO)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exportar/{formato}")
    @Operation(summary = "Exportar detalles de pedido", description = "Exporta los detalles de pedido en formato CSV o Excel usando un archivo reimportable")
    public ResponseEntity<byte[]> exportDetallesPedido(@PathVariable String formato) {
        return ImportExportResponseBuilder.build(
                importExportFacadeService.exportData("detalles-pedido", TabularFileFormat.from(formato))
        );
    }

    @PostMapping(value = "/importar/{formato}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar detalles de pedido", description = "Importa detalles de pedido desde un archivo CSV o Excel. Inserta o actualiza por id_detalle o por combinacion pedido-producto")
    public ResponseEntity<ImportResultDTO> importDetallesPedido(@PathVariable String formato,
                                                                @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                importExportFacadeService.importData("detalles-pedido", file, TabularFileFormat.from(formato))
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de pedido por ID", description = "Retorna los detalles de una linea especifica de pedido")
    public ResponseEntity<DetallePedidoDTO> getDetallePedidoById(@PathVariable Integer id) {
        return ResponseEntity.ok(detallePedidoMapper.toDTO(detallePedidoService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo detalle de pedido", description = "Agrega un detalle de forma puntual a un pedido existente. Para ventas completas se recomienda usar la operacion de pedidos")
    public ResponseEntity<DetallePedidoDTO> createDetallePedido(@Valid @RequestBody DetallePedidoDTO detallePedidoDTO) {
        DetallePedido saved = detallePedidoService.saveFromDto(detallePedidoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(detallePedidoMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar detalle de pedido", description = "Actualiza una linea puntual de detalle de pedido. Para actualizaciones completas se recomienda usar la operacion de pedidos")
    public ResponseEntity<DetallePedidoDTO> updateDetallePedido(@PathVariable Integer id, @Valid @RequestBody DetallePedidoDTO detallePedidoDTO) {
        detallePedidoDTO.setIdDetalle(id);
        DetallePedido saved = detallePedidoService.saveFromDto(detallePedidoDTO);
        return ResponseEntity.ok(detallePedidoMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar detalle de pedido", description = "Elimina una linea de detalle de pedido por su identificador")
    public ResponseEntity<Void> deleteDetallePedido(@PathVariable Integer id) {
        detallePedidoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
