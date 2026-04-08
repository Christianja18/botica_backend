package com.botica.botica.service;

import com.botica.botica.entity.Categoria;
import com.botica.botica.entity.Producto;
import com.botica.botica.entity.Proveedor;
import com.botica.botica.repository.CategoriaRepository;
import com.botica.botica.repository.ProductoRepository;
import com.botica.botica.repository.ProveedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProductoServiceTest {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    private Producto producto;
    private Categoria categoria;
    private Proveedor proveedor;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
        proveedorRepository.deleteAll();

        // Crear datos de prueba
        categoria = new Categoria();
        categoria.setNombre("Medicamentos");
        categoria.setDescripcion("Productos farmacéuticos");
        categoria = categoriaRepository.save(categoria);

        proveedor = new Proveedor();
        proveedor.setNombre("Proveedor ABC");
        proveedor.setRuc("12345678901");
        proveedor.setTelefono("987654321");
        proveedor.setEmail("proveedor@example.com");
        proveedor.setDireccion("Dirección");
        proveedor = proveedorRepository.save(proveedor);

        producto = new Producto();
        producto.setNombre("Paracetamol");
        producto.setDescripcion("Analgésico");
        producto.setPrecioVenta(BigDecimal.valueOf(5.00));
        producto.setPrecioCompra(BigDecimal.valueOf(3.50));
        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);
        producto.setRequiereReceta(false);
        producto = productoRepository.save(producto);
    }

    @Test
    void testFindAll() {
        List<Producto> result = productoService.findAll();
        assertEquals(1, result.size());
        assertEquals("Paracetamol", result.get(0).getNombre());
    }

    @Test
    void testFindById() {
        Producto result = productoService.findById(producto.getIdProducto());
        assertEquals("Paracetamol", result.getNombre());
    }

    @Test
    void testFindByNombre() {
        List<Producto> result = productoService.findByNombre("para");
        assertEquals(1, result.size());
        assertEquals("Paracetamol", result.get(0).getNombre());
    }

    @Test
    void testSave() {
        Producto newProducto = new Producto();
        newProducto.setNombre("Ibuprofeno");
        newProducto.setDescripcion("Antiinflamatorio");
        newProducto.setPrecioVenta(BigDecimal.valueOf(8.00));
        newProducto.setPrecioCompra(BigDecimal.valueOf(5.50));
        newProducto.setCategoria(categoria);
        newProducto.setProveedor(proveedor);
        newProducto.setRequiereReceta(false);

        Producto result = productoService.save(newProducto);
        assertNotNull(result);
        assertEquals("Ibuprofeno", result.getNombre());
    }

    @Test
    void testDeleteById() {
        productoService.deleteById(producto.getIdProducto());
        assertThrows(Exception.class, () -> productoService.findById(producto.getIdProducto()));
    }
}