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
class ProveedorDTOValidationTest {

    @Autowired
    private Validator validator;

    private ProveedorDTO validProveedor;

    @BeforeEach
    void setUp() {
        validProveedor = new ProveedorDTO();
        validProveedor.setNombre("PharmaPlus SAC");
        validProveedor.setRuc("20512345678");
        validProveedor.setTelefono("987654321");
        validProveedor.setEmail("contacto@pharmaplus.com");
        validProveedor.setDireccion("Av. Principal 123, Lima");
    }

    @Test
    void validProveedorPasaValidacion() {
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertTrue(violations.isEmpty(), "Proveedor valido no deberia tener violaciones");
    }

    @Test
    void proveedorSinNombreNoesValido() {
        validProveedor.setNombre("");
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "Proveedor sin nombre deberia fallar");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("nombre es obligatorio")));
    }

    @Test
    void proveedorConNombreMuyLargoNoesValido() {
        validProveedor.setNombre("A".repeat(151));
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "Nombre mayor a 150 caracteres deberia fallar");
    }

    @Test
    void proveedorConRucInvalidoNoesValido() {
        validProveedor.setRuc("12345");
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "RUC con menos de 11 digitos deberia fallar");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("exactamente 11 digitos")));
    }

    @Test
    void proveedorConRucNoNumericoNoesValido() {
        validProveedor.setRuc("2051234567A");
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "RUC con caracteres no numericos deberia fallar");
    }

    @Test
    void proveedorConEmailInvalidoNoesValido() {
        validProveedor.setEmail("email-invalido");
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "Email invalido deberia fallar");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("formato valido")));
    }

    @Test
    void proveedorConTelefonoNoNumericoNoesValido() {
        validProveedor.setTelefono("A87654321");
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "Telefono con caracteres no numericos deberia fallar");
    }

    @Test
    void proveedorConTelefonoDistintoANueveDigitosNoesValido() {
        validProveedor.setTelefono("98765432");
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "Telefono distinto a 9 digitos deberia fallar");
    }

    @Test
    void proveedorSinRucNoesValido() {
        validProveedor.setRuc("");
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertFalse(violations.isEmpty(), "Proveedor sin RUC deberia fallar");
    }

    @Test
    void proveedorConEmailNuloNoFalla() {
        validProveedor.setEmail(null);
        Set<ConstraintViolation<ProveedorDTO>> violations = validator.validate(validProveedor);
        assertTrue(violations.isEmpty(), "Email nulo deberia ser valido");
    }
}
