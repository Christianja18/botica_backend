package com.botica.botica.service;

import com.botica.botica.dto.ResumenPeriodoDTO;
import com.botica.botica.dto.ProductoMasVendidoDTO;
import com.botica.botica.entity.Pedido;
import com.botica.botica.entity.Reporte;
import com.botica.botica.entity.Usuario;
import com.botica.botica.repository.PedidoRepository;
import com.botica.botica.repository.ReporteRepository;
import com.botica.botica.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private ReporteService reporteService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        reporteService = new ReporteService(
                reporteRepository,
                pedidoRepository,
                usuarioRepository,
                objectMapper,
                entityManager
        );

        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombre("Juan");
        usuario.setApellido("Perez");
        usuario.setEmail("juan@example.com");
        usuario.setPasswordHash("hash");
        usuario.setActivo(true);
    }

    @Test
    void testFindAll() {
        when(reporteRepository.findAll()).thenReturn(List.of(new Reporte()));

        List<Reporte> result = reporteService.findAll();

        assertEquals(1, result.size());
        verify(reporteRepository).findAll();
    }

    @Test
    void testSave() {
        Reporte reporte = new Reporte();
        Reporte saved = new Reporte();
        saved.setIdReporte(1);

        when(reporteRepository.save(reporte)).thenReturn(saved);
        when(reporteRepository.findById(1)).thenReturn(Optional.of(saved));

        Reporte result = reporteService.save(reporte);

        assertNotNull(result);
        assertEquals(1, result.getIdReporte());
        verify(reporteRepository).save(reporte);
        verify(reporteRepository).findById(1);
    }

    @Test
    void testGenerarReporteVentas() throws Exception {
        LocalDateTime inicio = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 4, 30, 23, 59);

        Pedido pedido = new Pedido();
        pedido.setFechaPedido(LocalDateTime.of(2026, 4, 10, 10, 30));
        pedido.setTotal(new BigDecimal("150.50"));
        pedido.setEstado(Pedido.EstadoPedido.completado);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(pedidoRepository.findByFechaPedidoBetweenAndEstado(inicio, fin, Pedido.EstadoPedido.completado))
                .thenReturn(List.of(pedido));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"total\":150.50}");
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(invocation -> {
            Reporte saved = invocation.getArgument(0);
            saved.setIdReporte(1);
            return saved;
        });
        when(reporteRepository.findById(1)).thenAnswer(invocation -> {
            Reporte persisted = new Reporte();
            persisted.setIdReporte(1);
            persisted.setTipoReporte(Reporte.TipoReporte.ventas);
            persisted.setGeneradoPor(usuario);
            persisted.setDatos("{\"total\":150.50}");
            return Optional.of(persisted);
        });

        Reporte result = reporteService.generarReporteVentas(inicio, fin, 1);

        assertNotNull(result);
        assertEquals(Reporte.TipoReporte.ventas, result.getTipoReporte());
        assertEquals(usuario, result.getGeneradoPor());
        assertEquals("{\"total\":150.50}", result.getDatos());

        ArgumentCaptor<Reporte> captor = ArgumentCaptor.forClass(Reporte.class);
        verify(reporteRepository).save(captor.capture());
        assertEquals(Reporte.TipoReporte.ventas, captor.getValue().getTipoReporte());
        verify(pedidoRepository).findByFechaPedidoBetweenAndEstado(inicio, fin, Pedido.EstadoPedido.completado);
    }

    @Test
    void testGetProductosPorVencer() {
        String sql = "SELECT id_producto, codigo_barras, nombre, fecha_vencimiento, dias_para_vencer FROM vista_productos_por_vencer";
        when(entityManager.createNativeQuery(sql)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(new Object[]{1, "7751234567890", "Paracetamol", "2026-05-01", 22}));

        List<Map<String, Object>> result = reporteService.getProductosPorVencer();

        assertEquals(1, result.size());
        assertEquals("7751234567890", result.get(0).get("codigo_barras"));
        verify(entityManager).createNativeQuery(sql);
    }

    @Test
    void testGetProductosMasVendidos() {
        String sql = "SELECT id_producto, codigo_barras, nombre, id_categoria, categoria, cantidad_vendida, total_vendido FROM vista_productos_mas_vendidos";
        when(entityManager.createNativeQuery(sql)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(
                new Object[]{1, "7751234567890", "Paracetamol", 2, "Analgesicos", 14, new BigDecimal("70.00")}
        ));

        List<ProductoMasVendidoDTO> result = reporteService.getProductosMasVendidos();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).idProducto());
        assertEquals("Paracetamol", result.get(0).nombre());
        assertEquals("Analgesicos", result.get(0).categoria());
        assertEquals(14, result.get(0).cantidadVendida());
        assertEquals(new BigDecimal("70.00"), result.get(0).totalVendido());
        verify(entityManager).createNativeQuery(sql);
    }

    @Test
    void testGetGananciasResumenMensual() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(1, 2026)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(new Object[]{2026, 4, "04/2026", new BigDecimal("250.00")}));

        List<ResumenPeriodoDTO> result = reporteService.getGananciasResumen("mes", 2026);

        assertEquals(1, result.size());
        assertEquals(2026, result.get(0).anio());
        assertEquals(4, result.get(0).periodo());
        assertEquals("04/2026", result.get(0).etiqueta());
        assertEquals(new BigDecimal("250.00"), result.get(0).valor());

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("MONTH(p.fecha_pedido)"));
        assertTrue(sql.contains("SUM(dp.subtotal - (dp.cantidad * pr.precio_compra))"));
        verify(query).setParameter(eq(1), eq(2026));
    }

    @Test
    void testGetVentasResumenAnualGeneraGroupByCompatibleConOnlyFullGroupBy() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(new Object[]{2026, 2026, "2026", new BigDecimal("500.00")}));

        List<ResumenPeriodoDTO> result = reporteService.getVentasResumen("anio", null);

        assertEquals(1, result.size());
        assertEquals(2026, result.get(0).anio());
        assertEquals(2026, result.get(0).periodo());
        assertEquals("2026", result.get(0).etiqueta());
        assertEquals(new BigDecimal("500.00"), result.get(0).valor());

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("CAST(YEAR(p.fecha_pedido) AS CHAR)"));
        assertTrue(sql.contains("GROUP BY YEAR(p.fecha_pedido), CAST(YEAR(p.fecha_pedido) AS CHAR)"));
    }
}
