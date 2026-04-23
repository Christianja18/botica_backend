package com.botica.botica.integration;

import com.botica.botica.entity.Rol;
import com.botica.botica.entity.Usuario;
import com.botica.botica.repository.RolRepository;
import com.botica.botica.repository.UsuarioRepository;
import com.botica.botica.service.backup.DatabaseBackupResult;
import com.botica.botica.service.backup.DatabaseBackupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb-backup",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DatabaseBackupControllerIntegrationTest {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @MockBean
    private DatabaseBackupService databaseBackupService;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();
    }

    @Test
    void backupCompletoDebeGuardarArchivoEnDiscoConUsuarioAdministrador() throws Exception {
        seedUser("admin@botica.com", "Administrador", true);
        when(databaseBackupService.createManualFullBackup())
                .thenReturn(new DatabaseBackupResult(
                        "completo",
                        "botica_db-full.sql",
                        "C:\\copia\\botica_db-full.sql",
                        128L,
                        "Backup generado correctamente en C:\\copia"
                ));

        String token = login("admin@botica.com", "Botica2026!");

        mockMvc.perform(post("/api/backups/completo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("completo"))
                .andExpect(jsonPath("$.fileName").value("botica_db-full.sql"))
                .andExpect(jsonPath("$.absolutePath").value("C:\\copia\\botica_db-full.sql"));

        verify(databaseBackupService).createManualFullBackup();
    }

    @Test
    void backupCompletoDebeSerRechazadoSinPermisoDeAdministrarUsuarios() throws Exception {
        seedUser("reportes@botica.com", "Reportes", false);
        String token = login("reportes@botica.com", "Botica2026!");

        mockMvc.perform(post("/api/backups/completo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private void seedUser(String email, String rolNombre, boolean puedeAdministrarUsuarios) {
        Rol rol = new Rol();
        rol.setNombre(rolNombre);
        rol.setDescripcion("Rol de prueba");
        rol.setActivo(true);
        rol.setPuedeVender(true);
        rol.setPuedeAdministrarInventario(true);
        rol.setPuedeVerReportes(true);
        rol.setPuedeAdministrarUsuarios(puedeAdministrarUsuarios);
        rol = rolRepository.save(rol);

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario");
        usuario.setApellido("Prueba");
        usuario.setEmail(email);
        usuario.setPasswordHash(PASSWORD_ENCODER.encode("Botica2026!"));
        usuario.setActivo(true);
        usuario.setRol(rol);
        usuarioRepository.save(usuario);
    }

    private String login(String email, String password) throws Exception {
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
