package com.botica.botica.controller;

import com.botica.botica.dto.ReporteDTO;
import com.botica.botica.entity.Reporte;
import com.botica.botica.mapper.ReporteMapper;
import com.botica.botica.service.ReporteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<ReporteDTO>> getAllReportes() {
        return ResponseEntity.ok(reporteService.findAll().stream()
                .map(reporteMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReporteDTO> getReporteById(@PathVariable Integer id) {
        return ResponseEntity.ok(reporteMapper.toDTO(reporteService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ReporteDTO> createReporte(@Valid @RequestBody ReporteDTO reporteDTO) {
        Reporte reporte = reporteMapper.toEntity(reporteDTO);
        Reporte saved = reporteService.saveFromDto(reporte, reporteDTO.getGeneradoPor());
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteMapper.toDTO(saved));
    }

    @PostMapping("/generar/ventas")
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
    public ResponseEntity<ReporteDTO> generarReporteInventario(@RequestParam Integer idUsuario) {
        Reporte reporte = reporteService.generarReporteInventario(idUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteMapper.toDTO(reporte));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReporte(@PathVariable Integer id) {
        reporteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ventas-por-mes/{year}")
    public ResponseEntity<List<Map<String, Object>>> getVentasPorMes(@PathVariable int year) {
        return ResponseEntity.ok(reporteService.getVentasPorMes(year));
    }

    @GetMapping("/ganancias-por-mes/{year}")
    public ResponseEntity<List<Map<String, Object>>> getGananciasPorMes(@PathVariable int year) {
        return ResponseEntity.ok(reporteService.getGananciasPorMes(year));
    }

    @GetMapping("/inventario-bajo")
    public ResponseEntity<List<Map<String, Object>>> getInventarioBajo() {
        return ResponseEntity.ok(reporteService.getInventarioBajo());
    }
}
