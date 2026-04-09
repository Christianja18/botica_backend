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
class ClienteDTOValidationTest {

    @Autowired
    private Validator validator;

    private ClienteDTO validCliente;

    @BeforeEach
    void setUp() {
        validCliente = new ClienteDTO();
        validCliente.setNombre("Carlos");
        validCliente.setApellido("Garcia");
        validCliente.setDni("12345678");
        validCliente.setTelefono("987654321");
        validCliente.setEmail("carlos.garcia@mail.com");
    }

    @Test
    void validClientePasaValidacion() {
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertTrue(violations.isEmpty(), "Cliente valido no deberia tener violaciones");
    }

    @Test
    void clienteSinNombreNoesValido() {
        validCliente.setNombre("");
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "Cliente sin nombre deberia fallar");
    }

    @Test
    void clienteSinApellidoNoesValido() {
        validCliente.setApellido("");
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "Cliente sin apellido deberia fallar");
    }

    @Test
    void clienteConDniInvalidoNoesValido() {
        validCliente.setDni("1234567");
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "DNI con 7 digitos deberia fallar");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dni")));
    }

    @Test
    void clienteConDniNomericoNoesValido() {
        validCliente.setDni("1234567A");
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "DNI con caracteres no numericos deberia fallar");
    }

    @Test
    void clienteConDniTooLargNoesValido() {
        validCliente.setDni("123456789");
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "DNI con 9 digitos deberia fallar");
    }

    @Test
    void clienteConEmailInvalidoNoesValido() {
        validCliente.setEmail("email-sin-arroba");
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "Email sin @ deberia fallar");
    }

    @Test
    void clienteConNombreMuyLargoNoesValido() {
        validCliente.setNombre("A".repeat(101));
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertFalse(violations.isEmpty(), "Nombre mayor a 100 caracteres deberia fallar");
    }

    @Test
    void clienteConDniNuloNoFalla() {
        validCliente.setDni(null);
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertTrue(violations.isEmpty(), "DNI nulo deberia ser valido");
    }

    @Test
    void clienteConEmailNuloNoFalla() {
        validCliente.setEmail(null);
        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(validCliente);
        assertTrue(violations.isEmpty(), "Email nulo deberia ser valido");
    }
}
