package com.botica.botica.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PedidoDTOValidationTest {

    @Autowired
    private Validator validator;

    private PedidoDTO validPedido;

    @BeforeEach
    void setUp() {
        validPedido = new PedidoDTO();
        validPedido.setIdCliente(1);
        validPedido.setIdUsuario(1);
        validPedido.setTotal(BigDecimal.valueOf(100.00));
        validPedido.setEstado("pendiente");
    }

    @Test
    void validPedidoPasaValidacion() {
        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(validPedido);
        assertTrue(violations.isEmpty(), "Pedido válido no debería tener violaciones");
    }

    @Test
    void pedidoSinUsuarioNoesValido() {
        validPedido.setIdUsuario(null);
        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(validPedido);
        assertFalse(violations.isEmpty(), "Pedido sin usuario debería fallar");
    }

    @Test
    void pedidoSinTotalNoesValido() {
        validPedido.setTotal(null);
        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(validPedido);
        assertFalse(violations.isEmpty(), "Pedido sin total debería fallar");
    }

    @Test
    void pedidoConTotalNegativoNoesValido() {
        validPedido.setTotal(BigDecimal.valueOf(-1.00));
        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(validPedido);
        assertFalse(violations.isEmpty(), "Pedido con total negativo debería fallar");
    }

    @Test
    void pedidoSinEstadoNoesValido() {
        validPedido.setEstado(null);
        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(validPedido);
        assertFalse(violations.isEmpty(), "Pedido sin estado debería fallar");
    }
}