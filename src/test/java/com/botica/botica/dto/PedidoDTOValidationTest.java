package com.botica.botica.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(violations.isEmpty(), "Pedido valido no deberia tener violaciones");
    }

    @Test
    void pedidoSinUsuarioNoesValido() {
        validPedido.setIdUsuario(null);
        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(validPedido);
        assertFalse(violations.isEmpty(), "Pedido sin usuario deberia fallar");
    }

    @Test
    void pedidoSinTotalSigueSiendoValido() {
        validPedido.setTotal(null);
        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(validPedido);
        assertTrue(violations.isEmpty(), "Pedido sin total debe ser valido porque el backend lo deriva");
    }

    @Test
    void pedidoConTotalNegativoNoesValido() {
        validPedido.setTotal(BigDecimal.valueOf(-1.00));
        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(validPedido);
        assertFalse(violations.isEmpty(), "Pedido con total negativo deberia fallar");
    }

    @Test
    void pedidoSinEstadoNoesValido() {
        validPedido.setEstado(null);
        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(validPedido);
        assertFalse(violations.isEmpty(), "Pedido sin estado deberia fallar");
    }
}
