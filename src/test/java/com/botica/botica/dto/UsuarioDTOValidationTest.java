package com.botica.botica.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UsuarioDTOValidationTest {

    @Autowired
    private Validator validator;

    private UsuarioDTO validUsuario;

    @BeforeEach
    void setUp() {
        validUsuario = new UsuarioDTO();
        validUsuario.setNombre("Juan");
        validUsuario.setApellido("Pérez");
        validUsuario.setEmail("juan.perez@botica.com");
        validUsuario.setPasswordHash("SecurePassword123");
        validUsuario.setActivo(true);
        validUsuario.setIdRol(1);
    }

    @Test
    void validUsuarioPasaValidacion() {
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertTrue(violations.isEmpty(), "Usuario válido no debería tener violaciones");
    }

    @Test
    void usuarioSinNombreNoesValido() {
        validUsuario.setNombre("");
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Usuario sin nombre debería fallar");
    }

    @Test
    void usuarioSinApellidoNoesValido() {
        validUsuario.setApellido("");
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Usuario sin apellido debería fallar");
    }

    @Test
    void usuarioSinEmailNoesValido() {
        validUsuario.setEmail("");
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Usuario sin email debería fallar");
    }

    @Test
    void usuarioConEmailInvalidoNoesValido() {
        validUsuario.setEmail("email-sin-arroba");
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Email inválido debería fallar");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("formato válido")));
    }

    @Test
    void usuarioSinPasswordNoesValido() {
        validUsuario.setPasswordHash("");
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario, UsuarioDTO.OnCreate.class);
        assertFalse(violations.isEmpty(), "Usuario sin contraseña debería fallar");
    }

    @Test
    void usuarioConPasswordMuyCortoNoesValido() {
        // Test con contraseña de 8 caracteres (debería pasar)
        validUsuario.setPasswordHash("Pass1234"); // 8 caracteres
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario, UsuarioDTO.OnCreate.class);
        assertTrue(violations.isEmpty(), "Contraseña de 8 caracteres debería pasar");
        
        // Test con contraseña de 7 caracteres (debería fallar)
        UsuarioDTO usuarioCorto = new UsuarioDTO();
        usuarioCorto.setNombre("Juan");
        usuarioCorto.setApellido("Pérez");
        usuarioCorto.setEmail("juan.perez@botica.com");
        usuarioCorto.setPasswordHash("Pass12"); // 7 caracteres
        usuarioCorto.setActivo(true);
        usuarioCorto.setIdRol(1);
        
        violations = validator.validate(usuarioCorto, UsuarioDTO.OnCreate.class);
        assertFalse(violations.isEmpty(), "Contraseña menor a 8 caracteres debería fallar");
    }

    @Test
    void usuarioConNombreMuyLargoNoesValido() {
        validUsuario.setNombre("A".repeat(101));
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Nombre mayor a 100 caracteres debería fallar");
    }

    @Test
    void usuarioSinRolNoesValido() {
        validUsuario.setIdRol(null);
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Usuario sin rol debería fallar");
    }

    @Test
    void usuarioSinActivoNoesValido() {
        validUsuario.setActivo(null);
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Usuario sin estado activo debería fallar");
    }

    @Test
    void usuarioConEmailMuyLargoNoesValido() {
        validUsuario.setEmail("a".repeat(140) + "@test.com"); // Mayor a 150
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Email mayor a 150 caracteres debería fallar");
    }
}
