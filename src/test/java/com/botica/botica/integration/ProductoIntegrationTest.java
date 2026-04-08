package com.botica.botica.integration;

import com.botica.botica.entity.Categoria;
import com.botica.botica.entity.Producto;
import com.botica.botica.entity.Proveedor;
import com.botica.botica.entity.Rol;
import com.botica.botica.entity.Usuario;
import com.botica.botica.repository.CategoriaRepository;
import com.botica.botica.repository.ProductoRepository;
import com.botica.botica.repository.ProveedorRepository;
import com.botica.botica.repository.RolRepository;
import com.botica.botica.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProductoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    private Categoria testCategoria;
    private Proveedor testProveedor;
    private Producto testProducto;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
        proveedorRepository.deleteAll();

        Rol testRol = new Rol();
        testRol.setNombre("Admin");
        testRol.setDescripcion("Administrador");
        testRol.setActivo(true);
        testRol.setPuedeVender(true);
        testRol.setPuedeAdministrarInventario(true);
        testRol.setPuedeVerReportes(true);
        testRol.setPuedeAdministrarUsuarios(true);
        testRol = rolRepository.save(testRol);

        Usuario testUsuario = new Usuario();
        testUsuario.setNombre("Juan");
        testUsuario.setApellido("Perez");
        testUsuario.setEmail("juan@botica.com");
        testUsuario.setPasswordHash("SecurePassword123");
        testUsuario.setActivo(true);
        testUsuario.setRol(testRol);
        usuarioRepository.save(testUsuario);

        testCategoria = new Categoria();
        testCategoria.setNombre("Medicamentos");
        testCategoria.setDescripcion("Medicinas generales");
        testCategoria = categoriaRepository.save(testCategoria);

        testProveedor = new Proveedor();
        testProveedor.setNombre("Farmaceutica XYZ");
        testProveedor.setRuc("12345678901");
        testProveedor.setTelefono("987654321");
        testProveedor.setEmail("proveedor@botica.com");
        testProveedor.setDireccion("Calle Principal 123");
        testProveedor = proveedorRepository.save(testProveedor);

        testProducto = new Producto();
        testProducto.setNombre("Paracetamol");
        testProducto.setDescripcion("Analgesico y antipiretico");
        testProducto.setPrecioVenta(BigDecimal.valueOf(5.00));
        testProducto.setPrecioCompra(BigDecimal.valueOf(3.50));
        testProducto.setCategoria(testCategoria);
        testProducto.setProveedor(testProveedor);
        testProducto.setRequiereReceta(false);
        testProducto = productoRepository.save(testProducto);
    }

    @Test
    void testObtenerTodosLosProductos() throws Exception {
        mockMvc.perform(get("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Paracetamol"))
                .andExpect(jsonPath("$[0].precioVenta").value(5.00))
                .andExpect(jsonPath("$[0].precioCompra").value(3.50));
    }

    @Test
    void testObtenerProductoPorId() throws Exception {
        mockMvc.perform(get("/api/productos/" + testProducto.getIdProducto())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Paracetamol"))
                .andExpect(jsonPath("$.descripcion").value("Analgesico y antipiretico"));
    }

    @Test
    void testBuscarProductoPorNombre() throws Exception {
        mockMvc.perform(get("/api/productos/buscar/paracetamol")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Paracetamol"));
    }

    @Test
    void testCrearProducto() throws Exception {
        String nuevoProductoJson = "{"
                + "\"nombre\":\"Ibuprofeno\","
                + "\"descripcion\":\"Antiinflamatorio\","
                + "\"precioVenta\":8.00,"
                + "\"precioCompra\":5.50,"
                + "\"idCategoria\":" + testCategoria.getIdCategoria() + ","
                + "\"idProveedor\":" + testProveedor.getIdProveedor() + ","
                + "\"requiereReceta\":false"
                + "}";

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoProductoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ibuprofeno"))
                .andExpect(jsonPath("$.precioVenta").value(8.00));
    }

    @Test
    void testActualizarProducto() throws Exception {
        String productoActualizadoJson = "{"
                + "\"nombre\":\"Paracetamol 500mg\","
                + "\"descripcion\":\"Analgesico mejorado\","
                + "\"precioVenta\":6.00,"
                + "\"precioCompra\":4.00,"
                + "\"idCategoria\":" + testCategoria.getIdCategoria() + ","
                + "\"idProveedor\":" + testProveedor.getIdProveedor() + ","
                + "\"requiereReceta\":false"
                + "}";

        mockMvc.perform(put("/api/productos/" + testProducto.getIdProducto())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoActualizadoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Paracetamol 500mg"))
                .andExpect(jsonPath("$.precioVenta").value(6.00));
    }
}
