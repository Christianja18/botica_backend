package com.botica.botica.controller;

import com.botica.botica.entity.Reporte;
import com.botica.botica.entity.Usuario;
import com.botica.botica.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    public ResponseEntity<List<Reporte>> getAllReportes() {
        return ResponseEntity.ok(reporteService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reporte> getReporteById(@PathVariable Integer id) {
        Reporte reporte = reporteService.findById(id);
        return ResponseEntity.ok(reporte);
    }

    @PostMapping
    public ResponseEntity<Reporte> createReporte(@RequestBody Reporte reporte) {
        return ResponseEntity.ok(reporteService.save(reporte));
    }

    @PostMapping("/generar/ventas")
    public ResponseEntity<Reporte> generarReporteVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam Integer idUsuario) {
        // Placeholder: Obtener usuario; en prod, usar autenticación
        Usuario usuario = new Usuario(); // Simular
        usuario.setIdUsuario(idUsuario);
        Reporte reporte = reporteService.generarReporteVentas(fechaInicio, fechaFin, usuario);
        return ResponseEntity.ok(reporte);
    }

    @PostMapping("/generar/inventario")
    public ResponseEntity<Reporte> generarReporteInventario(@RequestParam Integer idUsuario) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        Reporte reporte = reporteService.generarReporteInventario(usuario);
        return ResponseEntity.ok(reporte);
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

    @GetMapping("/inventario-bajo")
    public ResponseEntity<List<Map<String, Object>>> getInventarioBajo() {
        return ResponseEntity.ok(reporteService.getInventarioBajo());
    }
}