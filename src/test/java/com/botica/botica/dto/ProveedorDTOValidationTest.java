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
class ProveedorDTOValidationTest {

    @Autowired
    private Validator validator;

    private ProveedorDTO validProveedor;

    @BeforeEach
    void setUp() {
        validProveedor = new ProveedorDTO();
        validProveedor.setNombre("PharmaPlus SAC");
        validProveedor.setRuc("20512345678");
        validProveedor.setTelefono("014567890");
        validProveedor.setEmail("contacto@pharmaplus.com");
        validProveedor.setDireccion("Av. Principal 123, Lima");
    }

    @Test
    void validProveedorPasaValidacion() {
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertTrue(violations.isEmpty(), "Proveedor válido no debería tener violaciones");
    }

    @Test
    void proveedorSinNombreNoesValido() {
        validProveedor.setNombre("");
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "Proveedor sin nombre debería fallar");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("nombre es obligatorio")));
    }

    @Test
    void proveedorConNombreMuyLargoNoesValido() {
        validProveedor.setNombre("A".repeat(151));
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "Nombre mayor a 150 caracteres debería fallar");
    }

    @Test
    void proveedorConRucInvalidoNoesValido() {
        validProveedor.setRuc("12345"); // Menos de 11 dígitos
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "RUC con menos de 11 dígitos debería fallar");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("exactamente 11 dígitos")));
    }

    @Test
    void proveedorConRucNoNumericoNoesValido() {
        validProveedor.setRuc("2051234567A"); // Contiene letra
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "RUC con caracteres no numéricos debería fallar");
    }

    @Test
    void proveedorConEmailInvalidoNoesValido() {
        validProveedor.setEmail("email-invalido");
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "Email inválido debería fallar");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("formato válido")));
    }

    @Test
    void proveedorConTelefonoMuyLargoNoesValido() {
        validProveedor.setTelefono("A".repeat(16));
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "Teléfono mayor a 15 caracteres debería fallar");
    }

    @Test
    void proveedorSinRucNoesValido() {
        validProveedor.setRuc("");
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "Proveedor sin RUC debería fallar");
    }

    @Test
    void proveedorConEmailNuloNoFalla() {
        validProveedor.setEmail(null);
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertTrue(violations.isEmpty(), "Email nulo debería ser válido (opcional)");
    }
}
