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
class ReporteDTOValidationTest {

    @Autowired
    private Validator validator;

    private ReporteDTO validReporte;

    @BeforeEach
    void setUp() {
        validReporte = new ReporteDTO();
        validReporte.setTipoReporte("ventas");
        validReporte.setGeneradoPor(1);
    }

    @Test
    void validReportePasaValidacion() {
        Set<ConstraintViolation<ReporteDTO>> violations = validator.validate(validReporte);
        assertTrue(violations.isEmpty(), "Reporte valido no deberia tener violaciones");
    }

    @Test
    void reporteSinTipoNoEsValido() {
        validReporte.setTipoReporte("");
        Set<ConstraintViolation<ReporteDTO>> violations = validator.validate(validReporte);
        assertFalse(violations.isEmpty(), "Reporte sin tipo deberia fallar");
    }

    @Test
    void reporteSinGeneradoPorNoEsValido() {
        validReporte.setGeneradoPor(null);
        Set<ConstraintViolation<ReporteDTO>> violations = validator.validate(validReporte);
        assertFalse(violations.isEmpty(), "Reporte sin usuario generador deberia fallar");
    }
}
