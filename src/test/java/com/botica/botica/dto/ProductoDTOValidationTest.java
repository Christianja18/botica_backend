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
class ProductoDTOValidationTest {

    @Autowired
    private Validator validator;

    private ProductoDTO validProducto;

    @BeforeEach
    void setUp() {
        validProducto = new ProductoDTO();
        validProducto.setNombre("Paracetamol 500mg");
        validProducto.setDescripcion("Tabletas para dolor y fiebre");
        validProducto.setPrecioVenta(new BigDecimal("5.50"));
        validProducto.setPrecioCompra(new BigDecimal("2.00"));
        validProducto.setIdCategoria(1);
        validProducto.setIdProveedor(1);
        validProducto.setRequiereReceta(false);
    }

    @Test
    void validProductoPasaValidacion() {
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertTrue(violations.isEmpty(), "Producto válido no debería tener violaciones");
    }

    @Test
    void productoSinNombreNoesValido() {
        validProducto.setNombre("");
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin nombre debería fallar");
    }

    @Test
    void productoConNombreMuyLargoNoesValido() {
        validProducto.setNombre("A".repeat(201));
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Nombre mayor a 200 caracteres debería fallar");
    }

    @Test
    void productoSinPrecioVentaNoesValido() {
        validProducto.setPrecioVenta(null);
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin precio venta debería fallar");
    }

    @Test
    void productoConPrecioVentaMenorACeroNoesValido() {
        validProducto.setPrecioVenta(new BigDecimal("0.00"));
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Precio venta de 0 debería fallar");
    }

    @Test
    void productoConPrecioVentaNegativoNoesValido() {
        validProducto.setPrecioVenta(new BigDecimal("-5.50"));
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Precio venta negativo debería fallar");
    }

    @Test
    void productoSinPrecioCompraNoesValido() {
        validProducto.setPrecioCompra(null);
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin precio compra debería fallar");
    }

    @Test
    void productoConPrecioCompraInvalidoNoesValido() {
        validProducto.setPrecioCompra(new BigDecimal("-2.00"));
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Precio compra negativo debería fallar");
    }

    @Test
    void productoSinCategoriaNoesValido() {
        validProducto.setIdCategoria(null);
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin categoría debería fallar");
    }

    @Test
    void productoSinProveedorNoesValido() {
        validProducto.setIdProveedor(null);
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin proveedor debería fallar");
    }

    @Test
    void productoSinRequiereRecetaNoesValido() {
        validProducto.setRequiereReceta(null);
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin requiereReceta debería fallar");
    }

    @Test
    void productoConPreciosGrandes() {
        validProducto.setPrecioVenta(new BigDecimal("99999999.99"));
        validProducto.setPrecioCompra(new BigDecimal("99999999.99"));
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertTrue(violations.isEmpty(), "Precios válidos con muchos dígitos deberían pasar");
    }

    @Test
    void productoConPreciosMasDeDosdecimales() {
        validProducto.setPrecioVenta(new BigDecimal("5.505"));
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Precio con más de 2 decimales debería fallar");
    }
}
