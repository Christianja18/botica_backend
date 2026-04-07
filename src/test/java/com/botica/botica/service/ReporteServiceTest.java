package com.botica.botica.service;

import com.botica.botica.entity.Reporte;
import com.botica.botica.entity.Usuario;
import com.botica.botica.repository.PedidoRepository;
import com.botica.botica.repository.ProductoRepository;
import com.botica.botica.repository.ReporteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ReporteService reporteService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuario = new Usuario(1, "Juan", "Perez", "juan@example.com", "hash", null, true, null);
    }

    @Test
    void testFindAll() {
        when(reporteRepository.findAll()).thenReturn(Arrays.asList(new Reporte()));
        List<Reporte> result = reporteService.findAll();
        assertEquals(1, result.size());
        verify(reporteRepository, times(1)).findAll();
    }

    @Test
    void testSave() {
        Reporte reporte = new Reporte();
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporte);
        Reporte result = reporteService.save(reporte);
        assertNotNull(result);
        verify(reporteRepository, times(1)).save(reporte);
    }

    @Test
    void testGenerarReporteVentas() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(reporteRepository.save(any(Reporte.class))).thenReturn(new Reporte());
        Reporte result = reporteService.generarReporteVentas(LocalDateTime.now(), LocalDateTime.now(), usuario);
        assertNotNull(result);
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }
}