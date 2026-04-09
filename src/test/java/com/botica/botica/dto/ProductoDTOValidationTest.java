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
class ProductoDTOValidationTest {

    @Autowired
    private Validator validator;

    private ProductoDTO validProducto;

    @BeforeEach
    void setUp() {
        validProducto = new ProductoDTO();
        validProducto.setNombre("Paracetamol 500mg");
        validProducto.setCodigoBarras("7751234567890");
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
        assertTrue(violations.isEmpty(), "Producto valido no deberia tener violaciones");
    }

    @Test
    void productoSinNombreNoesValido() {
        validProducto.setNombre("");
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin nombre deberia fallar");
    }

    @Test
    void productoConNombreMuyLargoNoesValido() {
        validProducto.setNombre("A".repeat(201));
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Nombre mayor a 200 caracteres deberia fallar");
    }

    @Test
    void productoSinCodigoBarrasNoesValido() {
        validProducto.setCodigoBarras(null);
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin codigo de barras deberia fallar");
    }

    @Test
    void productoSinPrecioVentaNoesValido() {
        validProducto.setPrecioVenta(null);
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin precio venta deberia fallar");
    }

    @Test
    void productoConPrecioVentaMenorACeroNoesValido() {
        validProducto.setPrecioVenta(new BigDecimal("0.00"));
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Precio venta de 0 deberia fallar");
    }

    @Test
    void productoConPrecioVentaNegativoNoesValido() {
        validProducto.setPrecioVenta(new BigDecimal("-5.50"));
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Precio venta negativo deberia fallar");
    }

    @Test
    void productoSinPrecioCompraNoesValido() {
        validProducto.setPrecioCompra(null);
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin precio compra deberia fallar");
    }

    @Test
    void productoConPrecioCompraInvalidoNoesValido() {
        validProducto.setPrecioCompra(new BigDecimal("-2.00"));
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Precio compra negativo deberia fallar");
    }

    @Test
    void productoSinCategoriaNoesValido() {
        validProducto.setIdCategoria(null);
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin categoria deberia fallar");
    }

    @Test
    void productoSinProveedorNoesValido() {
        validProducto.setIdProveedor(null);
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin proveedor deberia fallar");
    }

    @Test
    void productoSinRequiereRecetaNoesValido() {
        validProducto.setRequiereReceta(null);
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Producto sin requiereReceta deberia fallar");
    }

    @Test
    void productoConPreciosGrandes() {
        validProducto.setPrecioVenta(new BigDecimal("99999999.99"));
        validProducto.setPrecioCompra(new BigDecimal("99999999.99"));
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertTrue(violations.isEmpty(), "Precios validos con muchos digitos deberian pasar");
    }

    @Test
    void productoConPreciosMasDeDosDecimales() {
        validProducto.setPrecioVenta(new BigDecimal("5.505"));
        Set<ConstraintViolation<ProductoDTO>> violations = validator.validate(validProducto);
        assertFalse(violations.isEmpty(), "Precio con mas de 2 decimales deberia fallar");
    }
}
