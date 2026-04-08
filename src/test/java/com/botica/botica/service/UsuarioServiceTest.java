package com.botica.botica.service;

import com.botica.botica.entity.Rol;
import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.repository.RolRepository;
import com.botica.botica.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Usuario usuario;
    private Rol rol;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();

        rol = new Rol();
        rol.setNombre("Admin");
        rol.setDescripcion("Administrador");
        rol.setPuedeVender(true);
        rol.setPuedeAdministrarInventario(true);
        rol.setPuedeVerReportes(true);
        rol.setPuedeAdministrarUsuarios(true);
        rol.setActivo(true);
        rol = rolRepository.save(rol);

        usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellido("Perez");
        usuario.setEmail("juan@example.com");
        usuario.setPasswordHash("password123");
        usuario.setActivo(true);
        usuario.setRol(rol);
        usuario = usuarioRepository.save(usuario);
    }

    @Test
    void testFindAll() {
        List<Usuario> result = usuarioService.findAll();
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getNombre());
    }

    @Test
    void testFindById() {
        Usuario result = usuarioService.findById(usuario.getIdUsuario());
        assertEquals("Juan", result.getNombre());
    }

    @Test
    void testSave() {
        Usuario newUsuario = new Usuario();
        newUsuario.setNombre("Maria");
        newUsuario.setApellido("Garcia");
        newUsuario.setEmail("maria@example.com");
        newUsuario.setPasswordHash("password123");
        newUsuario.setActivo(true);
        newUsuario.setRol(rol);

        Usuario result = usuarioService.save(newUsuario);
        assertNotNull(result);
        assertEquals("Maria", result.getNombre());
        assertNotEquals("password123", result.getPasswordHash());
        assertTrue(result.getPasswordHash().startsWith("$2"));
    }

    @Test
    void testSaveEmailExists() {
        Usuario duplicateUsuario = new Usuario();
        duplicateUsuario.setNombre("Pedro");
        duplicateUsuario.setApellido("Lopez");
        duplicateUsuario.setEmail("juan@example.com");
        duplicateUsuario.setPasswordHash("password123");
        duplicateUsuario.setActivo(true);
        duplicateUsuario.setRol(rol);

        assertThrows(BadRequestException.class, () -> usuarioService.save(duplicateUsuario));
    }

    @Test
    void testDeleteById() {
        usuarioService.deleteById(usuario.getIdUsuario());
        assertThrows(Exception.class, () -> usuarioService.findById(usuario.getIdUsuario()));
    }
}
