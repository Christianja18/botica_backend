package com.botica.botica.service;

import com.botica.botica.entity.Rol;
import com.botica.botica.entity.Usuario;
import com.botica.botica.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private Rol rol;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rol = new Rol(1, "Admin", "Administrador", true, true, true, true, true, null);
        usuario = new Usuario(1, "Juan", "Perez", "juan@example.com", "hash", null, true, rol);
    }

    @Test
    void testFindAll() {
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario));
        List<Usuario> result = usuarioService.findAll();
        assertEquals(1, result.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        Usuario result = usuarioService.findById(1);
        assertEquals("Juan", result.getNombre());
    }

    @Test
    void testSave() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        Usuario result = usuarioService.save(usuario);
        assertNotNull(result);
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void testSave_EmailExists() {
        when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.save(usuario));
    }

    @Test
    void testDeleteById() {
        doNothing().when(usuarioRepository).deleteById(1);
        usuarioService.deleteById(1);
        verify(usuarioRepository, times(1)).deleteById(1);
    }
}