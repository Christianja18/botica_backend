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

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb-auth",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthControllerIntegrationTest {

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

        Rol rol = new Rol();
        rol.setNombre("Administrador");
        rol.setDescripcion("Acceso total");
        rol.setActivo(true);
        rol.setPuedeVender(true);
        rol.setPuedeAdministrarInventario(true);
        rol.setPuedeVerReportes(true);
        rol.setPuedeAdministrarUsuarios(true);
        rol = rolRepository.save(rol);

        Usuario usuario = new Usuario();
        usuario.setNombre("Maria");
        usuario.setApellido("Perez");
        usuario.setEmail("maria.perez@botica.com");
        usuario.setPasswordHash(PASSWORD_ENCODER.encode("Botica2026!"));
        usuario.setActivo(true);
        usuario.setRol(rol);
        usuarioRepository.save(usuario);
    }

    @Test
    void loginDebeRetornarTokenYSesion() throws Exception {
        String payload = """
                {
                  "email": "maria.perez@botica.com",
                  "password": "Botica2026!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token", not("")))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.usuario.email").value("maria.perez@botica.com"))
                .andExpect(jsonPath("$.usuario.rolNombre").value("Administrador"))
                .andExpect(jsonPath("$.usuario.puedeVerReportes").value(true));
    }

    @Test
    void loginDebeFallarConPasswordIncorrecto() throws Exception {
        String payload = """
                {
                  "email": "maria.perez@botica.com",
                  "password": "Incorrecta123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void meDebeRetornarSesionConTokenValido() throws Exception {
        String payload = """
                {
                  "email": "maria.perez@botica.com",
                  "password": "Botica2026!"
                }
                """;

        String token = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.nombreCompleto").value("Maria Perez"))
                .andExpect(jsonPath("$.usuario.puedeAdministrarUsuarios").value(true));
    }
}
