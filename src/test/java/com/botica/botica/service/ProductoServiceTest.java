package com.botica.botica.service;

import com.botica.botica.entity.Categoria;
import com.botica.botica.entity.Producto;
import com.botica.botica.entity.Proveedor;
import com.botica.botica.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private Categoria categoria;
    private Proveedor proveedor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoria = new Categoria(1, "Medicamentos", "Productos farmacéuticos");
        proveedor = new Proveedor(1, "Proveedor ABC", "12345678901", "987654321", "proveedor@example.com", "Dirección", null);
        producto = new Producto(1, "Paracetamol", "Analgésico", BigDecimal.valueOf(5.00), BigDecimal.valueOf(3.50), categoria, proveedor, false, null);
    }

    @Test
    void testFindAll() {
        when(productoRepository.findAll()).thenReturn(Arrays.asList(producto));
        List<Producto> result = productoService.findAll();
        assertEquals(1, result.size());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        Producto result = productoService.findById(1);
        assertEquals("Paracetamol", result.getNombre());
    }

    @Test
    void testFindByNombre() {
        when(productoRepository.findByNombreContainingIgnoreCase("para")).thenReturn(Arrays.asList(producto));
        List<Producto> result = productoService.findByNombre("para");
        assertEquals(1, result.size());
    }

    @Test
    void testSave() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        Producto result = productoService.save(producto);
        assertNotNull(result);
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void testDeleteById() {
        doNothing().when(productoRepository).deleteById(1);
        productoService.deleteById(1);
        verify(productoRepository, times(1)).deleteById(1);
    }
}