package com.botica.botica.integration;

import com.botica.botica.entity.Rol;
import com.botica.botica.entity.Usuario;
import com.botica.botica.repository.RolRepository;
import com.botica.botica.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb-security",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ApiAuthorizationIntegrationTest {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();

        Rol admin = new Rol();
        admin.setNombre("Administrador");
        admin.setDescripcion("Acceso total");
        admin.setActivo(true);
        admin.setPuedeVender(true);
        admin.setPuedeAdministrarInventario(true);
        admin.setPuedeVerReportes(true);
        admin.setPuedeAdministrarUsuarios(true);
        admin = rolRepository.save(admin);

        Rol vendedor = new Rol();
        vendedor.setNombre("Vendedor");
        vendedor.setDescripcion("Solo ventas");
        vendedor.setActivo(true);
        vendedor.setPuedeVender(true);
        vendedor.setPuedeAdministrarInventario(false);
        vendedor.setPuedeVerReportes(false);
        vendedor.setPuedeAdministrarUsuarios(false);
        vendedor = rolRepository.save(vendedor);

        Usuario adminUser = new Usuario();
        adminUser.setNombre("Maria");
        adminUser.setApellido("Perez");
        adminUser.setEmail("maria.perez@botica.com");
        adminUser.setPasswordHash(PASSWORD_ENCODER.encode("Botica2026!"));
        adminUser.setActivo(true);
        adminUser.setRol(admin);
        usuarioRepository.save(adminUser);

        Usuario sellerUser = new Usuario();
        sellerUser.setNombre("Juan");
        sellerUser.setApellido("Lopez");
        sellerUser.setEmail("juan.lopez@botica.com");
        sellerUser.setPasswordHash(PASSWORD_ENCODER.encode("Botica2026!"));
        sellerUser.setActivo(true);
        sellerUser.setRol(vendedor);
        usuarioRepository.save(sellerUser);
    }

    @Test
    void clientesDebeRequerirAutenticacion() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void vendedorNoDebeAccederAUsuarios() throws Exception {
        String token = loginAndExtractToken("juan.lopez@botica.com", "Botica2026!");

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorDebeAccederAClientes() throws Exception {
        String token = loginAndExtractToken("juan.lopez@botica.com", "Botica2026!");

        mockMvc.perform(get("/api/clientes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String loginAndExtractToken(String email, String password) throws Exception {
        String payload = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }
}
