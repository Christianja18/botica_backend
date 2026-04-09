package com.botica.botica.service;

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
import static org.mockito.ArgumentMatchers.any;
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
        when(reporteRepository.save(reporte)).thenReturn(reporte);

        Reporte result = reporteService.save(reporte);

        assertNotNull(result);
        verify(reporteRepository).save(reporte);
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
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
    void testGetGananciasPorMes() {
        String sql = "SELECT anio, mes, ganancia FROM vista_ganancias_mensuales WHERE anio = :year";
        when(entityManager.createNativeQuery(sql)).thenReturn(query);
        when(query.setParameter("year", 2026)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(new Object[]{2026, 4, new BigDecimal("250.00")}));

        List<Map<String, Object>> result = reporteService.getGananciasPorMes(2026);

        assertEquals(1, result.size());
        assertEquals(2026, result.get(0).get("anio"));
        assertEquals(4, result.get(0).get("mes"));
        assertEquals(new BigDecimal("250.00"), result.get(0).get("ganancia"));
        verify(query).setParameter(eq("year"), eq(2026));
    }
}
