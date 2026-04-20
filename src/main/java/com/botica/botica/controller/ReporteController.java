package com.botica.botica.controller;

import com.botica.botica.dto.PageResponseDTO;
import com.botica.botica.dto.ProductoMasVendidoDTO;
import com.botica.botica.dto.ReporteDTO;
import com.botica.botica.dto.ResumenPeriodoDTO;
import com.botica.botica.entity.Reporte;
import com.botica.botica.mapper.ReporteMapper;
import com.botica.botica.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Operaciones de reportes y estadisticas")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;
    private final ReporteMapper reporteMapper;

    @GetMapping
    @Operation(summary = "Obtener todos los reportes", description = "Retorna una lista de todos los reportes generados en el sistema")
    public ResponseEntity<List<ReporteDTO>> getAllReportes() {
        return ResponseEntity.ok(reporteService.findAll().stream()
                .map(reporteMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener reportes paginados", description = "Retorna una lista paginada de reportes generados en el sistema")
    public ResponseEntity<PageResponseDTO<ReporteDTO>> getReportesPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idReporte") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageResponseDTO<ReporteDTO> response = PageResponseDTO.from(
                reporteService.findAll(PageRequest.of(page, size, sort)).map(reporteMapper::toDTO)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reporte por ID", description = "Retorna los detalles de un reporte especifico")
    public ResponseEntity<ReporteDTO> getReporteById(@PathVariable Integer id) {
        return ResponseEntity.ok(reporteMapper.toDTO(reporteService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo reporte", description = "Crea un nuevo registro de reporte a partir de los datos enviados")
    public ResponseEntity<ReporteDTO> createReporte(@Valid @RequestBody ReporteDTO reporteDTO) {
        Reporte reporte = reporteMapper.toEntity(reporteDTO);
        Reporte saved = reporteService.saveFromDto(reporte, reporteDTO.getGeneradoPor());
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteMapper.toDTO(saved));
    }

    @PostMapping("/generar/ventas")
    @Operation(summary = "Generar reporte de ventas", description = "Genera un reporte de ventas completadas dentro de un rango de fechas")
    public ResponseEntity<ReporteDTO> generarReporteVentas(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin,
            @RequestParam Integer idUsuario) {
        Reporte reporte = reporteService.generarReporteVentas(
                fechaInicio.atStartOfDay(),
                fechaFin.atTime(23, 59, 59),
                idUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteMapper.toDTO(reporte));
    }

    @PostMapping("/generar/inventario")
    @Operation(summary = "Generar reporte de inventario", description = "Genera un reporte con informacion del inventario actual y productos con stock bajo")
    public ResponseEntity<ReporteDTO> generarReporteInventario(@RequestParam Integer idUsuario) {
        Reporte reporte = reporteService.generarReporteInventario(idUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteMapper.toDTO(reporte));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reporte", description = "Elimina un reporte registrado por su identificador")
    public ResponseEntity<Void> deleteReporte(@PathVariable Integer id) {
        reporteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ventas-resumen")
    @Operation(summary = "Obtener resumen de ventas por periodo", description = "Retorna el resumen de ventas agrupado por dia, mes, anio, bimestral, trimestral, semestral o anual consolidado")
    public ResponseEntity<List<ResumenPeriodoDTO>> getVentasResumen(
            @RequestParam String agrupacion,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(reporteService.getVentasResumen(agrupacion, year));
    }

    @GetMapping("/ganancias-resumen")
    @Operation(summary = "Obtener resumen de ganancias por periodo", description = "Retorna el resumen de ganancias agrupado por dia, mes, anio, bimestral, trimestral, semestral o anual consolidado")
    public ResponseEntity<List<ResumenPeriodoDTO>> getGananciasResumen(
            @RequestParam String agrupacion,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(reporteService.getGananciasResumen(agrupacion, year));
    }

    @GetMapping("/inventario-bajo")
    @Operation(summary = "Obtener productos con inventario bajo", description = "Retorna una lista de productos cuyo stock actual es menor al stock minimo")
    public ResponseEntity<List<Map<String, Object>>> getInventarioBajo() {
        return ResponseEntity.ok(reporteService.getInventarioBajo());
    }

    @GetMapping("/productos-por-vencer")
    @Operation(summary = "Obtener productos por vencer", description = "Retorna una lista de productos cuya fecha de vencimiento esta dentro de los proximos 30 dias")
    public ResponseEntity<List<Map<String, Object>>> getProductosPorVencer() {
        return ResponseEntity.ok(reporteService.getProductosPorVencer());
    }

    @GetMapping("/productos-vencidos")
    @Operation(summary = "Obtener productos vencidos", description = "Retorna una lista de productos cuya fecha de vencimiento ya paso")
    public ResponseEntity<List<Map<String, Object>>> getProductosVencidos() {
        return ResponseEntity.ok(reporteService.getProductosVencidos());
    }

    @GetMapping("/productos-mas-vendidos")
    @Operation(summary = "Obtener productos mas vendidos", description = "Retorna el ranking de productos mas vendidos basado en pedidos completados")
    public ResponseEntity<List<ProductoMasVendidoDTO>> getProductosMasVendidos() {
        return ResponseEntity.ok(reporteService.getProductosMasVendidos());
    }
}
