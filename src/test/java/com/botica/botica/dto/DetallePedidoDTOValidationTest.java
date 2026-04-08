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
class DetallePedidoDTOValidationTest {

    @Autowired
    private Validator validator;

    private DetallePedidoDTO validDetalle;

    @BeforeEach
    void setUp() {
        validDetalle = new DetallePedidoDTO();
        validDetalle.setIdPedido(1);
        validDetalle.setIdProducto(1);
        validDetalle.setCantidad(2);
        validDetalle.setPrecioUnitario(BigDecimal.valueOf(5.00));
        validDetalle.setSubtotal(BigDecimal.valueOf(10.00));
    }

    @Test
    void validDetallePedidoPasaValidacion() {
        Set<ConstraintViolation<DetallePedidoDTO>> violations = validator.validate(validDetalle);
        assertTrue(violations.isEmpty(), "Detalle de pedido válido no debería tener violaciones");
    }

    @Test
    void detallePedidoSinPedidoNoesValido() {
        validDetalle.setIdPedido(null);
        Set<ConstraintViolation<DetallePedidoDTO>> violations = validator.validate(validDetalle);
        assertFalse(violations.isEmpty(), "Detalle sin pedido debería fallar");
    }

    @Test
    void detallePedidoSinProductoNoesValido() {
        validDetalle.setIdProducto(null);
        Set<ConstraintViolation<DetallePedidoDTO>> violations = validator.validate(validDetalle);
        assertFalse(violations.isEmpty(), "Detalle sin producto debería fallar");
    }

    @Test
    void detallePedidoSinCantidadNoesValido() {
        validDetalle.setCantidad(null);
        Set<ConstraintViolation<DetallePedidoDTO>> violations = validator.validate(validDetalle);
        assertFalse(violations.isEmpty(), "Detalle sin cantidad debería fallar");
    }

    @Test
    void detallePedidoConCantidadCeroNoesValido() {
        validDetalle.setCantidad(0);
        Set<ConstraintViolation<DetallePedidoDTO>> violations = validator.validate(validDetalle);
        assertFalse(violations.isEmpty(), "Detalle con cantidad cero debería fallar");
    }

    @Test
    void detallePedidoSinPrecioUnitarioNoesValido() {
        validDetalle.setPrecioUnitario(null);
        Set<ConstraintViolation<DetallePedidoDTO>> violations = validator.validate(validDetalle);
        assertFalse(violations.isEmpty(), "Detalle sin precio unitario debería fallar");
    }

    @Test
    void detallePedidoConPrecioUnitarioNegativoNoesValido() {
        validDetalle.setPrecioUnitario(BigDecimal.valueOf(-1.00));
        Set<ConstraintViolation<DetallePedidoDTO>> violations = validator.validate(validDetalle);
        assertFalse(violations.isEmpty(), "Detalle con precio unitario negativo debería fallar");
    }

    @Test
    void detallePedidoConSubtotalNegativoNoesValido() {
        validDetalle.setSubtotal(BigDecimal.valueOf(-1.00));
        Set<ConstraintViolation<DetallePedidoDTO>> violations = validator.validate(validDetalle);
        assertFalse(violations.isEmpty(), "Detalle con subtotal negativo debería fallar");
    }
}