package com.botica.botica.service;

import com.botica.botica.entity.Pedido;
import com.botica.botica.entity.Reporte;
import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.PedidoRepository;
import com.botica.botica.repository.ReporteRepository;
import com.botica.botica.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
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
    public Reporte saveFromDto(Reporte reporte, Integer generadoPorId) {
        reporte.setGeneradoPor(resolveUsuario(generadoPorId));
        return reporteRepository.save(reporte);
    }

    @Transactional
    public void deleteById(Integer id) {
        reporteRepository.deleteById(id);
    }

    public Reporte generarReporteVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer generadoPorId) {
        Usuario generadoPor = resolveUsuario(generadoPorId);

        List<Object[]> ventas = pedidoRepository.findByFechaPedidoBetweenAndEstado(
                        fechaInicio,
                        fechaFin,
                        Pedido.EstadoPedido.completado
                )
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
            return save(reporte);
        } catch (Exception e) {
            throw new RuntimeException("Error generando reporte de ventas", e);
        }
    }

    public Reporte generarReporteInventario(Integer generadoPorId) {
        Usuario generadoPor = resolveUsuario(generadoPorId);
        List<Map<String, Object>> inventarioBajo = getInventarioBajo();
        Map<String, Object> datos = Map.of("inventario_bajo", inventarioBajo);

        try {
            String datosJson = objectMapper.writeValueAsString(datos);
            Reporte reporte = new Reporte();
            reporte.setTipoReporte(Reporte.TipoReporte.inventario);
            reporte.setGeneradoPor(generadoPor);
            reporte.setDatos(datosJson);
            return save(reporte);
        } catch (Exception e) {
            throw new RuntimeException("Error generando reporte de inventario", e);
        }
    }

    public List<Map<String, Object>> getVentasPorMes(int year) {
        String sql = "SELECT anio, mes, total_ventas FROM vista_ventas_mensuales WHERE anio = :year";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(row -> Map.of("anio", row[0], "mes", row[1], "total", row[2]))
                .toList();
    }

    public List<Map<String, Object>> getGananciasPorMes(int year) {
        String sql = "SELECT anio, mes, ganancia FROM vista_ganancias_mensuales WHERE anio = :year";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(row -> Map.of("anio", row[0], "mes", row[1], "ganancia", row[2]))
                .toList();
    }

    public List<Map<String, Object>> getInventarioBajo() {
        String sql = "SELECT nombre, stock_actual, stock_minimo FROM vista_inventario_bajo";
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(row -> Map.of("nombre", row[0], "stock_actual", row[1], "stock_minimo", row[2]))
                .toList();
    }

    private Usuario resolveUsuario(Integer usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));
    }
}
