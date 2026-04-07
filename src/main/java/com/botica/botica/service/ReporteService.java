package com.botica.botica.service;

import com.botica.botica.entity.Reporte;
import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.PedidoRepository;
import com.botica.botica.repository.ProductoRepository;
import com.botica.botica.repository.ReporteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    public List<Reporte> findAll() {
        return reporteRepository.findAll();
    }

    public Reporte findById(Integer id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con id: " + id));
    }

    @Transactional
    public Reporte save(Reporte reporte) {
        return reporteRepository.save(reporte);
    }

    @Transactional
    public void deleteById(Integer id) {
        reporteRepository.deleteById(id);
    }

    // Método para generar reporte de ventas
    public Reporte generarReporteVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin, Usuario generadoPor) {
        List<Object[]> ventas = pedidoRepository.findByFechaPedidoBetween(fechaInicio, fechaFin)
                .stream()
                .map(p -> new Object[]{p.getFechaPedido(), p.getTotal()})
                .toList();

        BigDecimal totalVentas = ventas.stream()
                .map(v -> (BigDecimal) v[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> datos = Map.of(
                "ventas", ventas,
                "total", totalVentas
        );

        try {
            String datosJson = objectMapper.writeValueAsString(datos);
            Reporte reporte = new Reporte();
            reporte.setTipoReporte(Reporte.TipoReporte.ventas);
            reporte.setFechaInicio(fechaInicio);
            reporte.setFechaFin(fechaFin);
            reporte.setGeneradoPor(generadoPor);
            reporte.setDatos(datosJson);
            // archivoPath se puede setear después de generar PDF
            return save(reporte);
        } catch (Exception e) {
            throw new RuntimeException("Error generando reporte", e);
        }
    }

    // Método para generar reporte de inventario (productos con stock bajo)
    public Reporte generarReporteInventario(Usuario generadoPor) {
        // Asumir lógica simple; en prod, query custom
        List<Object[]> inventario = productoRepository.findAll()
                .stream()
                .map(p -> new Object[]{p.getNombre(), "stock_placeholder"})  // Placeholder
                .toList();

        Map<String, Object> datos = Map.of("inventario", inventario);

        try {
            String datosJson = objectMapper.writeValueAsString(datos);
            Reporte reporte = new Reporte();
            reporte.setTipoReporte(Reporte.TipoReporte.inventario);
            reporte.setGeneradoPor(generadoPor);
            reporte.setDatos(datosJson);
            return save(reporte);
        } catch (Exception e) {
            throw new RuntimeException("Error generando reporte", e);
        }
    }

    // Método para datos de ventas por mes (usando vista)
    public List<Map<String, Object>> getVentasPorMes(int year) {
        String sql = "SELECT anio, mes, total_ventas FROM vista_ventas_mensuales WHERE anio = :year";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        List<Object[]> results = query.getResultList();
        return results.stream().map(row -> Map.of("anio", row[0], "mes", row[1], "total", row[2])).toList();
    }

    // Método para ganancias por mes (usando vista)
    public List<Map<String, Object>> getGananciasPorMes(int year) {
        String sql = "SELECT anio, mes, ganancia FROM vista_ganancias_mensuales WHERE anio = :year";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        List<Object[]> results = query.getResultList();
        return results.stream().map(row -> Map.of("anio", row[0], "mes", row[1], "ganancia", row[2])).toList();
    }

    // Nuevo método para inventario bajo (usando vista)
    public List<Map<String, Object>> getInventarioBajo() {
        String sql = "SELECT nombre, stock_actual, stock_minimo FROM vista_inventario_bajo";
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        return results.stream().map(row -> Map.of("nombre", row[0], "stock_actual", row[1], "stock_minimo", row[2])).toList();
    }
}