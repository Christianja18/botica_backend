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
class RolDTOValidationTest {

    @Autowired
    private Validator validator;

    private RolDTO validRol;

    @BeforeEach
    void setUp() {
        validRol = new RolDTO();
        validRol.setNombre("Admin");
        validRol.setDescripcion("Administrador del sistema");
        validRol.setPuedeVender(true);
        validRol.setPuedeAdministrarInventario(true);
        validRol.setPuedeVerReportes(true);
        validRol.setPuedeAdministrarUsuarios(true);
        validRol.setActivo(true);
    }

    @Test
    void validRolPasaValidacion() {
        Set<ConstraintViolation<RolDTO>> violations = validator.validate(validRol);
        assertTrue(violations.isEmpty(), "Rol válido no debería tener violaciones");
    }

    @Test
    void rolSinNombreNoesValido() {
        validRol.setNombre("");
        Set<ConstraintViolation<RolDTO>> violations = validator.validate(validRol);
        assertFalse(violations.isEmpty(), "Rol sin nombre debería fallar");
    }

    @Test
    void rolConNombreMuyLargoNoesValido() {
        validRol.setNombre("a".repeat(51));
        Set<ConstraintViolation<RolDTO>> violations = validator.validate(validRol);
        assertFalse(violations.isEmpty(), "Rol con nombre muy largo debería fallar");
    }

    @Test
    void rolConDescripcionMuyLargaNoesValido() {
        validRol.setDescripcion("a".repeat(256));
        Set<ConstraintViolation<RolDTO>> violations = validator.validate(validRol);
        assertFalse(violations.isEmpty(), "Rol con descripción muy larga debería fallar");
    }

    @Test
    void rolSinPuedeVenderNoesValido() {
        validRol.setPuedeVender(null);
        Set<ConstraintViolation<RolDTO>> violations = validator.validate(validRol);
        assertFalse(violations.isEmpty(), "Rol sin permiso vender debería fallar");
    }

    @Test
    void rolSinPuedeAdministrarInventarioNoesValido() {
        validRol.setPuedeAdministrarInventario(null);
        Set<ConstraintViolation<RolDTO>> violations = validator.validate(validRol);
        assertFalse(violations.isEmpty(), "Rol sin permiso administrar inventario debería fallar");
    }

    @Test
    void rolSinPuedeVerReportesNoesValido() {
        validRol.setPuedeVerReportes(null);
        Set<ConstraintViolation<RolDTO>> violations = validator.validate(validRol);
        assertFalse(violations.isEmpty(), "Rol sin permiso ver reportes debería fallar");
    }

    @Test
    void rolSinPuedeAdministrarUsuariosNoesValido() {
        validRol.setPuedeAdministrarUsuarios(null);
        Set<ConstraintViolation<RolDTO>> violations = validator.validate(validRol);
        assertFalse(violations.isEmpty(), "Rol sin permiso administrar usuarios debería fallar");
    }

    @Test
    void rolSinActivoNoesValido() {
        validRol.setActivo(null);
        Set<ConstraintViolation<RolDTO>> violations = validator.validate(validRol);
        assertFalse(violations.isEmpty(), "Rol sin estado activo debería fallar");
    }
}