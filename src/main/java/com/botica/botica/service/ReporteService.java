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
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private static final String SALES_SOURCE = " FROM pedidos p WHERE p.estado = 'completado'";
    private static final String GAINS_SOURCE = " FROM pedidos p" +
            " JOIN detalle_pedidos dp ON p.id_pedido = dp.id_pedido" +
            " JOIN productos pr ON dp.id_producto = pr.id_producto" +
            " WHERE p.estado = 'completado'";
    private static final String PEDIDO_DATE_COLUMN = "p.fecha_pedido";

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
        return mapNativeQuery(querySpec, this::toResumenPeriodoDTO);
    }

    public List<ResumenPeriodoDTO> getGananciasResumen(String agrupacion, Integer year) {
        AgrupacionPeriodo agrupacionPeriodo = AgrupacionPeriodo.from(agrupacion);
        QuerySpec querySpec = buildGainSummaryQuery(agrupacionPeriodo, year);
        return mapNativeQuery(querySpec, this::toResumenPeriodoDTO);
    }

    public List<Map<String, Object>> getInventarioBajo() {
        return mapNativeQuery(
                new QuerySpec("SELECT nombre, stock_actual, stock_minimo FROM vista_inventario_bajo"),
                row -> Map.of("nombre", row[0], "stock_actual", row[1], "stock_minimo", row[2])
        );
    }

    public List<Map<String, Object>> getProductosPorVencer() {
        return mapNativeQuery(
                new QuerySpec("SELECT id_producto, codigo_barras, nombre, fecha_vencimiento, dias_para_vencer FROM vista_productos_por_vencer"),
                row -> Map.of(
                        "id_producto", row[0],
                        "codigo_barras", row[1],
                        "nombre", row[2],
                        "fecha_vencimiento", row[3],
                        "dias_para_vencer", row[4]
                )
        );
    }

    public List<Map<String, Object>> getProductosVencidos() {
        return mapNativeQuery(
                new QuerySpec("SELECT id_producto, codigo_barras, nombre, fecha_vencimiento FROM vista_productos_vencidos"),
                row -> Map.of(
                        "id_producto", row[0],
                        "codigo_barras", row[1],
                        "nombre", row[2],
                        "fecha_vencimiento", row[3]
                )
        );
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

    private <T> List<T> mapNativeQuery(QuerySpec querySpec, Function<Object[], T> mapper) {
        return executeNativeQuery(querySpec.sql(), querySpec.parameters()).stream()
                .map(mapper)
                .toList();
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
                SALES_SOURCE
        );
    }

    private QuerySpec buildGainSummaryQuery(AgrupacionPeriodo agrupacion, Integer year) {
        return buildPeriodSummaryQuery(
                agrupacion,
                year,
                "COALESCE(SUM(dp.subtotal - (dp.cantidad * pr.precio_compra)), 0.00)",
                GAINS_SOURCE
        );
    }

    private QuerySpec buildPeriodSummaryQuery(AgrupacionPeriodo agrupacion, Integer year, String valueExpression, String fromClause) {
        List<Object> parameters = new ArrayList<>();
        String filterClause = "";

        if (agrupacion.requiresYear()) {
            agrupacion.requireYear(year);
            filterClause = " AND YEAR(" + PEDIDO_DATE_COLUMN + ") = ?1";
            parameters.add(year);
        }

        String selectClause = "SELECT YEAR(" + PEDIDO_DATE_COLUMN + ") AS anio, " +
                agrupacion.periodExpression(PEDIDO_DATE_COLUMN) + " AS periodo, " +
                agrupacion.labelExpression(PEDIDO_DATE_COLUMN) + " AS etiqueta, " +
                valueExpression + " AS valor";
        String sql = selectClause
                + fromClause
                + filterClause
                + " GROUP BY " + String.join(", ", agrupacion.groupByExpressions(PEDIDO_DATE_COLUMN))
                + " ORDER BY " + String.join(", ", agrupacion.orderByExpressions(PEDIDO_DATE_COLUMN));
        return new QuerySpec(sql, parameters.toArray());
    }

    private record QuerySpec(String sql, Object[] parameters) {
        private QuerySpec(String sql) {
            this(sql, new Object[0]);
        }
    }

    public enum AgrupacionPeriodo {
        DIA(
                "dia",
                true,
                "DAYOFYEAR({d})",
                "DATE_FORMAT({d}, '%d/%m/%Y')",
                List.of("YEAR({d})", "DAYOFYEAR({d})", "DATE({d})", "DATE_FORMAT({d}, '%d/%m/%Y')"),
                List.of("DATE({d}) ASC")
        ),
        MES(
                "mes",
                true,
                "MONTH({d})",
                "CONCAT(LPAD(MONTH({d}), 2, '0'), '/', YEAR({d}))",
                List.of("YEAR({d})", "MONTH({d})", "CONCAT(LPAD(MONTH({d}), 2, '0'), '/', YEAR({d}))"),
                List.of("YEAR({d}) ASC", "MONTH({d}) ASC")
        ),
        ANIO(
                "anio",
                false,
                "YEAR({d})",
                "CAST(YEAR({d}) AS CHAR)",
                List.of("YEAR({d})", "CAST(YEAR({d}) AS CHAR)"),
                List.of("YEAR({d}) ASC")
        ),
        BIMESTRAL(
                "bimestral",
                true,
                "CEIL(MONTH({d}) / 2.0)",
                "CONCAT('Bimestre ', CEIL(MONTH({d}) / 2.0), ' - ', YEAR({d}))",
                List.of("YEAR({d})", "CEIL(MONTH({d}) / 2.0)", "CONCAT('Bimestre ', CEIL(MONTH({d}) / 2.0), ' - ', YEAR({d}))"),
                List.of("YEAR({d}) ASC", "CEIL(MONTH({d}) / 2.0) ASC")
        ),
        TRIMESTRAL(
                "trimestral",
                true,
                "QUARTER({d})",
                "CONCAT('Trimestre ', QUARTER({d}), ' - ', YEAR({d}))",
                List.of("YEAR({d})", "QUARTER({d})", "CONCAT('Trimestre ', QUARTER({d}), ' - ', YEAR({d}))"),
                List.of("YEAR({d}) ASC", "QUARTER({d}) ASC")
        ),
        SEMESTRAL(
                "semestral",
                true,
                "CASE WHEN MONTH({d}) <= 6 THEN 1 ELSE 2 END",
                "CONCAT('Semestre ', CASE WHEN MONTH({d}) <= 6 THEN 1 ELSE 2 END, ' - ', YEAR({d}))",
                List.of("YEAR({d})", "CASE WHEN MONTH({d}) <= 6 THEN 1 ELSE 2 END", "CONCAT('Semestre ', CASE WHEN MONTH({d}) <= 6 THEN 1 ELSE 2 END, ' - ', YEAR({d}))"),
                List.of("YEAR({d}) ASC", "CASE WHEN MONTH({d}) <= 6 THEN 1 ELSE 2 END ASC")
        ),
        ANUAL_CONSOLIDADO(
                "anual_consolidado",
                true,
                "1",
                "CONCAT('Anual consolidado ', YEAR({d}))",
                List.of("YEAR({d})", "CONCAT('Anual consolidado ', YEAR({d}))"),
                List.of("YEAR({d}) ASC")
        );

        private final String value;
        private final boolean requiresYear;
        private final String periodExpressionTemplate;
        private final String labelExpressionTemplate;
        private final List<String> groupByTemplates;
        private final List<String> orderByTemplates;

        AgrupacionPeriodo(String value,
                          boolean requiresYear,
                          String periodExpressionTemplate,
                          String labelExpressionTemplate,
                          List<String> groupByTemplates,
                          List<String> orderByTemplates) {
            this.value = value;
            this.requiresYear = requiresYear;
            this.periodExpressionTemplate = periodExpressionTemplate;
            this.labelExpressionTemplate = labelExpressionTemplate;
            this.groupByTemplates = groupByTemplates;
            this.orderByTemplates = orderByTemplates;
        }

        public String value() {
            return value;
        }

        public boolean requiresYear() {
            return requiresYear;
        }

        public String periodExpression(String dateColumn) {
            return applyDateColumn(periodExpressionTemplate, dateColumn);
        }

        public String labelExpression(String dateColumn) {
            return applyDateColumn(labelExpressionTemplate, dateColumn);
        }

        public List<String> groupByExpressions(String dateColumn) {
            return groupByTemplates.stream()
                    .map(template -> applyDateColumn(template, dateColumn))
                    .toList();
        }

        public List<String> orderByExpressions(String dateColumn) {
            return orderByTemplates.stream()
                    .map(template -> applyDateColumn(template, dateColumn))
                    .toList();
        }

        public void requireYear(Integer year) {
            if (requiresYear && year == null) {
                throw new BadRequestException("El parametro year es obligatorio para la agrupacion " + value);
            }
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

        private static String applyDateColumn(String template, String dateColumn) {
            return template.replace("{d}", dateColumn);
        }
    }
}
