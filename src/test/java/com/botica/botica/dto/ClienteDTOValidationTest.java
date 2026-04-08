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
class ClienteDTOValidationTest {

    @Autowired
    private Validator validator;

    private ClienteDTO validCliente;

    @BeforeEach
    void setUp() {
        validCliente = new ClienteDTO();
        validCliente.setNombre("Carlos");
        validCliente.setApellido("García");
        validCliente.setDni("12345678");
        validCliente.setTelefono("987654321");
        validCliente.setEmail("carlos.garcia@mail.com");
    }

    @Test
    void validClientePasaValidacion() {
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertTrue(violations.isEmpty(), "Cliente válido no debería tener violaciones");
    }

    @Test
    void clienteSinNombreNoesValido() {
        validCliente.setNombre("");
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "Cliente sin nombre debería fallar");
    }

    @Test
    void clienteSinApellidoNoesValido() {
        validCliente.setApellido("");
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "Cliente sin apellido debería fallar");
    }

    @Test
    void clienteConDniInvalidoNoesValido() {
        validCliente.setDni("1234567"); // Solo 7 dígitos
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "DNI con 7 dígitos debería fallar");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("exactamente 8 dígitos")));
    }

    @Test
    void clienteConDniNomericoNoesValido() {
        validCliente.setDni("1234567A");
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "DNI con caracteres no numéricos debería fallar");
    }

    @Test
    void clienteConDniTooLargNoesValido() {
        validCliente.setDni("123456789");
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "DNI con 9 dígitos debería fallar");
    }

    @Test
    void clienteConEmailInvalidoNoesValido() {
        validCliente.setEmail("email-sin-arroba");
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "Email sin @ debería fallar");
    }

    @Test
    void clienteConNombreMuyLargoNoesValido() {
        validCliente.setNombre("A".repeat(101));
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "Nombre mayor a 100 caracteres debería fallar");
    }

    @Test
    void clienteConDniNuloNoFalla() {
        validCliente.setDni(null);
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertTrue(violations.isEmpty(), "DNI nulo debería ser válido (opcional)");
    }

    @Test
    void clienteConEmailNuloNoFalla() {
        validCliente.setEmail(null);
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertTrue(violations.isEmpty(), "Email nulo debería ser válido (opcional)");
    }
}
