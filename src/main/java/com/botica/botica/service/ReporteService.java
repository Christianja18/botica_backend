package com.botica.botica.service;

import com.botica.botica.entity.Pedido;
import com.botica.botica.entity.Reporte;
import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.PedidoRepository;
import com.botica.botica.repository.ReporteRepository;
import com.botica.botica.repository.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<Reporte> findAll(Pageable pageable) {
        return reporteRepository.findAll(pageable);
    }

    public Reporte findById(Integer id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con id: " + id));
    }

    @Transactional
    public Reporte save(Reporte reporte) {
        Reporte saved = reporteRepository.save(reporte);
        return findById(saved.getIdReporte());
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

        return saveReport(Reporte.TipoReporte.ventas, fechaInicio, fechaFin, generadoPor, datos, "Error generando reporte de ventas");
    }

    public Reporte generarReporteInventario(Integer generadoPorId) {
        Usuario generadoPor = resolveUsuario(generadoPorId);
        List<Map<String, Object>> inventarioBajo = getInventarioBajo();
        Map<String, Object> datos = Map.of("inventario_bajo", inventarioBajo);

        return saveReport(Reporte.TipoReporte.inventario, null, null, generadoPor, datos, "Error generando reporte de inventario");
    }

    public List<Map<String, Object>> getVentasPorMes(int year) {
        return executeNativeQuery("SELECT anio, mes, total_ventas FROM vista_ventas_mensuales WHERE anio = ?1", year).stream()
                .map(row -> Map.of("anio", row[0], "mes", row[1], "total", row[2]))
                .toList();
    }

    public List<Map<String, Object>> getGananciasPorMes(int year) {
        return executeNativeQuery("SELECT anio, mes, ganancia FROM vista_ganancias_mensuales WHERE anio = ?1", year).stream()
                .map(row -> Map.of("anio", row[0], "mes", row[1], "ganancia", row[2]))
                .toList();
    }

    public List<Map<String, Object>> getInventarioBajo() {
        return executeNativeQuery("SELECT nombre, stock_actual, stock_minimo FROM vista_inventario_bajo").stream()
                .map(row -> Map.of("nombre", row[0], "stock_actual", row[1], "stock_minimo", row[2]))
                .toList();
    }

    public List<Map<String, Object>> getProductosPorVencer() {
        return executeNativeQuery("SELECT id_producto, codigo_barras, nombre, fecha_vencimiento, dias_para_vencer FROM vista_productos_por_vencer").stream()
                .map(row -> Map.of(
                        "id_producto", row[0],
                        "codigo_barras", row[1],
                        "nombre", row[2],
                        "fecha_vencimiento", row[3],
                        "dias_para_vencer", row[4]
                ))
                .toList();
    }

    public List<Map<String, Object>> getProductosVencidos() {
        return executeNativeQuery("SELECT id_producto, codigo_barras, nombre, fecha_vencimiento FROM vista_productos_vencidos").stream()
                .map(row -> Map.of(
                        "id_producto", row[0],
                        "codigo_barras", row[1],
                        "nombre", row[2],
                        "fecha_vencimiento", row[3]
                ))
                .toList();
    }

    private Reporte saveReport(Reporte.TipoReporte tipoReporte,
                               LocalDateTime fechaInicio,
                               LocalDateTime fechaFin,
                               Usuario generadoPor,
                               Map<String, Object> datos,
                               String errorMessage) {
        try {
            Reporte reporte = new Reporte();
            reporte.setTipoReporte(tipoReporte);
            reporte.setFechaInicio(fechaInicio);
            reporte.setFechaFin(fechaFin);
            reporte.setGeneradoPor(generadoPor);
            reporte.setDatos(writeJson(datos));
            return save(reporte);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(errorMessage, ex);
        }
    }

    private String writeJson(Map<String, Object> datos) {
        try {
            return objectMapper.writeValueAsString(datos);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("No se pudo serializar el contenido del reporte", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> executeNativeQuery(String sql, Object... parameters) {
        Query query = entityManager.createNativeQuery(sql);
        for (int index = 0; index < parameters.length; index++) {
            query.setParameter(index + 1, parameters[index]);
        }
        return query.getResultList();
    }

    private Usuario resolveUsuario(Integer usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));
    }
}
