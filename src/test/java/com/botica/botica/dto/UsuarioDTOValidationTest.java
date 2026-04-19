package com.botica.botica.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UsuarioDTOValidationTest {

    @Autowired
    private Validator validator;

    private UsuarioDTO validUsuario;

    @BeforeEach
    void setUp() {
        validUsuario = new UsuarioDTO();
        validUsuario.setNombre("Juan");
        validUsuario.setApellido("Perez");
        validUsuario.setEmail("juan.perez@botica.com");
        validUsuario.setPasswordHash("SecurePassword123");
        validUsuario.setActivo(true);
        validUsuario.setIdRol(1);
    }

    @Test
    void validUsuarioPasaValidacion() {
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertTrue(violations.isEmpty(), "Usuario valido no deberia tener violaciones");
    }

    @Test
    void usuarioSinNombreNoesValido() {
        validUsuario.setNombre("");
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Usuario sin nombre deberia fallar");
    }

    @Test
    void usuarioSinApellidoNoesValido() {
        validUsuario.setApellido("");
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Usuario sin apellido deberia fallar");
    }

    @Test
    void usuarioSinEmailNoesValido() {
        validUsuario.setEmail("");
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Usuario sin email deberia fallar");
    }

    @Test
    void usuarioConEmailInvalidoNoesValido() {
        validUsuario.setEmail("email-sin-arroba");
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Email invalido deberia fallar");
        assertTrue(violations.stream().anyMatch(v -> "email".equals(v.getPropertyPath().toString())));
    }

    @Test
    void usuarioSinPasswordNoesValido() {
        validUsuario.setPasswordHash("");
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario, UsuarioDTO.OnCreate.class);
        assertFalse(violations.isEmpty(), "Usuario sin contrasena deberia fallar");
    }

    @Test
    void usuarioConPasswordMuyCortoNoesValido() {
        validUsuario.setPasswordHash("Pass1234");
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario, UsuarioDTO.OnCreate.class);
        assertTrue(violations.isEmpty(), "Contrasena de 8 caracteres deberia pasar");

        UsuarioDTO usuarioCorto = new UsuarioDTO();
        usuarioCorto.setNombre("Juan");
        usuarioCorto.setApellido("Perez");
        usuarioCorto.setEmail("juan.perez@botica.com");
        usuarioCorto.setPasswordHash("Pass12");
        usuarioCorto.setActivo(true);
        usuarioCorto.setIdRol(1);

        violations = validator.validate(usuarioCorto, UsuarioDTO.OnCreate.class);
        assertFalse(violations.isEmpty(), "Contrasena menor a 8 caracteres deberia fallar");
    }

    @Test
    void usuarioConNombreMuyLargoNoesValido() {
        validUsuario.setNombre("A".repeat(101));
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Nombre mayor a 100 caracteres deberia fallar");
    }

    @Test
    void usuarioSinRolNoesValido() {
        validUsuario.setIdRol(null);
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Usuario sin rol deberia fallar");
    }

    @Test
    void usuarioSinActivoNoesValido() {
        validUsuario.setActivo(null);
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Usuario sin estado activo deberia fallar");
    }

    @Test
    void usuarioConEmailMuyLargoNoesValido() {
        validUsuario.setEmail("a".repeat(140) + "@test.com");
        Set<ConstraintViolation<UsuarioDTO>> violations = validator.validate(validUsuario);
        assertFalse(violations.isEmpty(), "Email mayor a 150 caracteres deberia fallar");
    }
}
