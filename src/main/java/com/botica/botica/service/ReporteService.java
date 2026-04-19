package com.botica.botica.service;

import com.botica.botica.entity.Pedido;
import com.botica.botica.entity.Reporte;
import com.botica.botica.entity.Usuario;
import com.botica.botica.dto.ResumenPeriodoDTO;
import com.botica.botica.exception.BadRequestException;
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
import java.util.ArrayList;
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

    public List<ResumenPeriodoDTO> getVentasResumen(String agrupacion, Integer year) {
        AgrupacionPeriodo agrupacionPeriodo = AgrupacionPeriodo.from(agrupacion);
        QuerySpec querySpec = buildSalesSummaryQuery(agrupacionPeriodo, year);
        return executeNativeQuery(querySpec.sql(), querySpec.parameters()).stream()
                .map(this::toResumenPeriodoDTO)
                .toList();
    }

    public List<ResumenPeriodoDTO> getGananciasResumen(String agrupacion, Integer year) {
        AgrupacionPeriodo agrupacionPeriodo = AgrupacionPeriodo.from(agrupacion);
        QuerySpec querySpec = buildGainSummaryQuery(agrupacionPeriodo, year);
        return executeNativeQuery(querySpec.sql(), querySpec.parameters()).stream()
                .map(this::toResumenPeriodoDTO)
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

    private ResumenPeriodoDTO toResumenPeriodoDTO(Object[] row) {
        return new ResumenPeriodoDTO(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).intValue(),
                String.valueOf(row[2]),
                (BigDecimal) row[3]
        );
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

    private QuerySpec buildSalesSummaryQuery(AgrupacionPeriodo agrupacion, Integer year) {
        return buildPeriodSummaryQuery(
                agrupacion,
                year,
                "COALESCE(SUM(p.total), 0.00)",
                " FROM pedidos p WHERE p.estado = 'completado'"
        );
    }

    private QuerySpec buildGainSummaryQuery(AgrupacionPeriodo agrupacion, Integer year) {
        return buildPeriodSummaryQuery(
                agrupacion,
                year,
                "COALESCE(SUM(dp.subtotal - (dp.cantidad * pr.precio_compra)), 0.00)",
                " FROM pedidos p" +
                        " JOIN detalle_pedidos dp ON p.id_pedido = dp.id_pedido" +
                        " JOIN productos pr ON dp.id_producto = pr.id_producto" +
                        " WHERE p.estado = 'completado'"
        );
    }

    private QuerySpec buildPeriodSummaryQuery(AgrupacionPeriodo agrupacion, Integer year, String valueExpression, String fromClause) {
        String dateColumn = "p.fecha_pedido";
        List<Object> parameters = new ArrayList<>();
        String selectClause;
        String groupByClause;
        String orderByClause;
        String filterClause = "";

        switch (agrupacion) {
            case DIA -> {
                requireYear(year, agrupacion);
                filterClause = " AND YEAR(" + dateColumn + ") = ?1";
                parameters.add(year);
                selectClause = "SELECT YEAR(" + dateColumn + ") AS anio, " +
                        "DAYOFYEAR(" + dateColumn + ") AS periodo, " +
                        "DATE_FORMAT(" + dateColumn + ", '%d/%m/%Y') AS etiqueta, " +
                        valueExpression + " AS valor";
                groupByClause = " GROUP BY YEAR(" + dateColumn + "), DAYOFYEAR(" + dateColumn + "), DATE(" + dateColumn + "), " +
                        "DATE_FORMAT(" + dateColumn + ", '%d/%m/%Y')";
                orderByClause = " ORDER BY DATE(" + dateColumn + ") ASC";
            }
            case MES -> {
                requireYear(year, agrupacion);
                filterClause = " AND YEAR(" + dateColumn + ") = ?1";
                parameters.add(year);
                selectClause = "SELECT YEAR(" + dateColumn + ") AS anio, " +
                        "MONTH(" + dateColumn + ") AS periodo, " +
                        "CONCAT(LPAD(MONTH(" + dateColumn + "), 2, '0'), '/', YEAR(" + dateColumn + ")) AS etiqueta, " +
                        valueExpression + " AS valor";
                groupByClause = " GROUP BY YEAR(" + dateColumn + "), MONTH(" + dateColumn + "), " +
                        "CONCAT(LPAD(MONTH(" + dateColumn + "), 2, '0'), '/', YEAR(" + dateColumn + "))";
                orderByClause = " ORDER BY YEAR(" + dateColumn + ") ASC, MONTH(" + dateColumn + ") ASC";
            }
            case ANIO -> {
                selectClause = "SELECT YEAR(" + dateColumn + ") AS anio, " +
                        "YEAR(" + dateColumn + ") AS periodo, " +
                        "CAST(YEAR(" + dateColumn + ") AS CHAR) AS etiqueta, " +
                        valueExpression + " AS valor";
                groupByClause = " GROUP BY YEAR(" + dateColumn + "), CAST(YEAR(" + dateColumn + ") AS CHAR)";
                orderByClause = " ORDER BY YEAR(" + dateColumn + ") ASC";
            }
            case BIMESTRAL -> {
                requireYear(year, agrupacion);
                filterClause = " AND YEAR(" + dateColumn + ") = ?1";
                parameters.add(year);
                selectClause = "SELECT YEAR(" + dateColumn + ") AS anio, " +
                        "CEIL(MONTH(" + dateColumn + ") / 2.0) AS periodo, " +
                        "CONCAT('Bimestre ', CEIL(MONTH(" + dateColumn + ") / 2.0), ' - ', YEAR(" + dateColumn + ")) AS etiqueta, " +
                        valueExpression + " AS valor";
                groupByClause = " GROUP BY YEAR(" + dateColumn + "), CEIL(MONTH(" + dateColumn + ") / 2.0), " +
                        "CONCAT('Bimestre ', CEIL(MONTH(" + dateColumn + ") / 2.0), ' - ', YEAR(" + dateColumn + "))";
                orderByClause = " ORDER BY YEAR(" + dateColumn + ") ASC, CEIL(MONTH(" + dateColumn + ") / 2.0) ASC";
            }
            case TRIMESTRAL -> {
                requireYear(year, agrupacion);
                filterClause = " AND YEAR(" + dateColumn + ") = ?1";
                parameters.add(year);
                selectClause = "SELECT YEAR(" + dateColumn + ") AS anio, " +
                        "QUARTER(" + dateColumn + ") AS periodo, " +
                        "CONCAT('Trimestre ', QUARTER(" + dateColumn + "), ' - ', YEAR(" + dateColumn + ")) AS etiqueta, " +
                        valueExpression + " AS valor";
                groupByClause = " GROUP BY YEAR(" + dateColumn + "), QUARTER(" + dateColumn + "), " +
                        "CONCAT('Trimestre ', QUARTER(" + dateColumn + "), ' - ', YEAR(" + dateColumn + "))";
                orderByClause = " ORDER BY YEAR(" + dateColumn + ") ASC, QUARTER(" + dateColumn + ") ASC";
            }
            case SEMESTRAL -> {
                requireYear(year, agrupacion);
                filterClause = " AND YEAR(" + dateColumn + ") = ?1";
                parameters.add(year);
                selectClause = "SELECT YEAR(" + dateColumn + ") AS anio, " +
                        "CASE WHEN MONTH(" + dateColumn + ") <= 6 THEN 1 ELSE 2 END AS periodo, " +
                        "CONCAT('Semestre ', CASE WHEN MONTH(" + dateColumn + ") <= 6 THEN 1 ELSE 2 END, ' - ', YEAR(" + dateColumn + ")) AS etiqueta, " +
                        valueExpression + " AS valor";
                groupByClause = " GROUP BY YEAR(" + dateColumn + "), CASE WHEN MONTH(" + dateColumn + ") <= 6 THEN 1 ELSE 2 END, " +
                        "CONCAT('Semestre ', CASE WHEN MONTH(" + dateColumn + ") <= 6 THEN 1 ELSE 2 END, ' - ', YEAR(" + dateColumn + "))";
                orderByClause = " ORDER BY YEAR(" + dateColumn + ") ASC, CASE WHEN MONTH(" + dateColumn + ") <= 6 THEN 1 ELSE 2 END ASC";
            }
            case ANUAL_CONSOLIDADO -> {
                requireYear(year, agrupacion);
                filterClause = " AND YEAR(" + dateColumn + ") = ?1";
                parameters.add(year);
                selectClause = "SELECT YEAR(" + dateColumn + ") AS anio, " +
                        "1 AS periodo, " +
                        "CONCAT('Anual consolidado ', YEAR(" + dateColumn + ")) AS etiqueta, " +
                        valueExpression + " AS valor";
                groupByClause = " GROUP BY YEAR(" + dateColumn + "), CONCAT('Anual consolidado ', YEAR(" + dateColumn + "))";
                orderByClause = " ORDER BY YEAR(" + dateColumn + ") ASC";
            }
            default -> throw new BadRequestException("Agrupacion de reporte no soportada");
        }

        String sql = selectClause + fromClause + filterClause + groupByClause + orderByClause;
        return new QuerySpec(sql, parameters.toArray());
    }

    private void requireYear(Integer year, AgrupacionPeriodo agrupacion) {
        if (year == null) {
            throw new BadRequestException("El parametro year es obligatorio para la agrupacion " + agrupacion.value());
        }
    }

    private record QuerySpec(String sql, Object[] parameters) {
    }

    public enum AgrupacionPeriodo {
        DIA("dia"),
        MES("mes"),
        ANIO("anio"),
        BIMESTRAL("bimestral"),
        TRIMESTRAL("trimestral"),
        SEMESTRAL("semestral"),
        ANUAL_CONSOLIDADO("anual_consolidado");

        private final String value;

        AgrupacionPeriodo(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        public static AgrupacionPeriodo from(String rawValue) {
            if (rawValue == null || rawValue.isBlank()) {
                throw new BadRequestException("La agrupacion del reporte es obligatoria");
            }

            for (AgrupacionPeriodo agrupacion : values()) {
                if (agrupacion.value.equalsIgnoreCase(rawValue.trim())) {
                    return agrupacion;
                }
            }

            throw new BadRequestException("Agrupacion de reporte invalida: " + rawValue);
        }
    }
}
