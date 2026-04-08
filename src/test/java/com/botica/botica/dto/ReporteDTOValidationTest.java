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
class ReporteDTOValidationTest {

    @Autowired
    private Validator validator;

    private ReporteDTO validReporte;

    @BeforeEach
    void setUp() {
        validReporte = new ReporteDTO();
        validReporte.setTipoReporte("ventas");
        validReporte.setGeneradoPor(1);
        validReporte.setArchivoPath("/path/to/report.pdf");
    }

    @Test
    void validReportePasaValidacion() {
        Set<ConstraintViolation<ReporteDTO>> violations = validator.validate(validReporte);
        assertTrue(violations.isEmpty(), "Reporte válido no debería tener violaciones");
    }

    @Test
    void reporteSinTipoNoesValido() {
        validReporte.setTipoReporte("");
        Set<ConstraintViolation<ReporteDTO>> violations = validator.validate(validReporte);
        assertFalse(violations.isEmpty(), "Reporte sin tipo debería fallar");
    }

    @Test
    void reporteSinGeneradoPorNoesValido() {
        validReporte.setGeneradoPor(null);
        Set<ConstraintViolation<ReporteDTO>> violations = validator.validate(validReporte);
        assertFalse(violations.isEmpty(), "Reporte sin usuario generador debería fallar");
    }

    @Test
    void reporteConArchivoPathMuyLargoNoesValido() {
        validReporte.setArchivoPath("a".repeat(501));
        Set<ConstraintViolation<ReporteDTO>> violations = validator.validate(validReporte);
        assertFalse(violations.isEmpty(), "Reporte con archivo path muy largo debería fallar");
    }
}